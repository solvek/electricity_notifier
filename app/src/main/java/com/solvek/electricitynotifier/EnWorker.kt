package com.solvek.electricitynotifier

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.hasKeyWithValueOfType
import com.solvek.electricitynotifier.EnApp.Companion.enApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit
import kotlin.time.toDuration

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
            Log.d("Worker", "Work disabled. Exiting.")
            return Result.success()
        }

        Log.d("EnWorker", "Work started")
        val isOn = newStatus?.also {
            model.setCurrentStatus(it)
        } ?: model.isOn
        return withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val recent = model.record

            if (recent.on == isOn){
                model.log("New status and current status are the same: $isOn")
                return@withContext Result.success()
            }

            val duration = (now - recent.time).toDuration(DurationUnit.MILLISECONDS)
            if (duration > 3.minutes){
                model.log("Sending notification")
                try {
                    actuator.notify(isOn, duration)
                }
                catch (th: Throwable){
                    Log.e("EnWorker", "Failed to notify", th)
                    model.log("Notification failed")
                    return@withContext Result.retry()
                }
                model.registerAction(now, isOn)
                model.log("Notification sent")
                return@withContext Result.success()
            }

            model.log("Change too quickly")
            return@withContext Result.retry()
        }
    }

    companion object {
//        fun Context.schedulePeriodic(){
//            workManager.enqueue(
////                WORK_NAME,
////                ExistingPeriodicWorkPolicy.KEEP,
//                PeriodicWorkRequestBuilder<EnWorker>(15, TimeUnit.MINUTES)
//                    .build()
//            )
//        }

        fun Context.syncPowerState(isOn: Boolean) {
            enApp.log("Power state changed: $isOn")
            val data: Data = Data.Builder()
                .putBoolean(ARGUMENT_IS_ON, isOn)
                .build()

            syncPowerState {
                setInputData(data)
            }
        }

        fun Context.syncPowerState() = syncPowerState {  }

        private fun Context.syncPowerState(builder: OneTimeWorkRequest.Builder.()->Unit){
            val requestBuilder = OneTimeWorkRequest.Builder(EnWorker::class.java)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())

            requestBuilder.builder()

            workManager.enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                requestBuilder.build()
            )
        }

        private val Context.workManager get() = WorkManager.getInstance(this)

        private const val ARGUMENT_IS_ON = "ARGUMENT_IS_ON"
        private const val WORK_NAME = "EnWorker"
    }
}