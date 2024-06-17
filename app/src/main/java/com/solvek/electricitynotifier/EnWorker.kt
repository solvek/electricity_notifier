package com.solvek.electricitynotifier

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.solvek.electricitynotifier.EnApp.Companion.enApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class EnWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    private val model by lazy { applicationContext.enApp }
    private val actuator by lazy { Actuator() }

    override suspend fun doWork(): Result  {
        if (!model.enabled.value){
            Log.d("Worker", "Work disabled. Exiting.")
            return Result.success()
        }

        Log.d("EnWorker", "Work started")
        val isOn = model.isOn
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
        fun Context.syncPowerState(){
            val requestBuilder = OneTimeWorkRequest.Builder(EnWorker::class.java)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())

            workManager.enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                requestBuilder.build()
            )
        }

        private val Context.workManager get() = WorkManager.getInstance(this)

        private const val WORK_NAME = "EnWorker"
    }
}