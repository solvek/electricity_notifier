package com.solvek.electricitynotifier

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.hasKeyWithValueOfType
import com.solvek.electricitynotifier.EnApp.Companion.enApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

class EnWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    private val model by lazy { applicationContext.enApp }
    private val actuator by lazy { Actuator() }
    private val newStatus by lazy {
        val data = workerParams.inputData
        if (data.hasKeyWithValueOfType<Boolean>(ARGUMENT_IS_ON)){
            data.getBoolean(ARGUMENT_IS_ON, false)
        }
        else {
            null
        }
    }

    override suspend fun doWork(): Result  {
        if (!model.enabled.value){
            return Result.success()
        }
        Log.d("EnWorker", "Work started")
        return withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val isOn = newStatus?.also {
                model.setCurrentStatus(it)
            } ?: model.isOn
            val recent = model.record

            if (recent.on == isOn){
                return@withContext Result.success()
            }

            val duration = now - recent.time
            if (duration > 5.seconds.inWholeMilliseconds){
                actuator.notify(isOn, duration)
                model.registerAction(now, isOn)
            }
            else {
                model.log("Change too quickly")
            }

            return@withContext Result.success()
        }
    }

    companion object {
        fun Context.schedulePeriodic(){
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<EnWorker>(15, TimeUnit.MINUTES)
                    .build()
            )
        }

        fun Context.handlePowerState(isOn: Boolean){
            enApp.log("Power state changed: $isOn")
            val data: Data = Data.Builder()
                .putBoolean(ARGUMENT_IS_ON, isOn)
                .build()

            workManager.enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.KEEP,
                OneTimeWorkRequest.Builder(EnWorker::class.java)
                    .setInputData(data)
                    .build()
            )
        }

        private val Context.workManager get() = WorkManager.getInstance(this)

        private const val ARGUMENT_IS_ON = "ARGUMENT_IS_ON"
        private const val WORK_NAME = "EnWorker"
    }
}