package com.solvek.electricitynotifier

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import androidx.work.hasKeyWithValueOfType
import com.solvek.electricitynotifier.EnApp.Companion.enApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes

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

            if (now - recent.time > 5.minutes.inWholeMinutes){
                actuator.notify(isOn)
                model.registerAction(now, isOn)
            }

            return@withContext Result.success()
        }
    }

    companion object {
        fun Context.schedulePeriodic(){
            enqueue(
                PeriodicWorkRequestBuilder<EnWorker>(15, TimeUnit.MINUTES)
                    .build()
            )
        }

        fun Context.handlePowerState(isOn: Boolean){
            enApp.log("Power state changed: $isOn")
            val data: Data = Data.Builder()
                .putBoolean(ARGUMENT_IS_ON, isOn)
                .build()

            enqueue(
                OneTimeWorkRequest.Builder(EnWorker::class.java)
                    .setInputData(data)
                    .build()
            )
        }

        private fun Context.enqueue(request: WorkRequest) {
            WorkManager.getInstance(this).enqueue(request)
        }

        private const val ARGUMENT_IS_ON = "ARGUMENT_IS_ON"
    }
}