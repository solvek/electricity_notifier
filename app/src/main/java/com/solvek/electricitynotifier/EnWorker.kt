package com.solvek.electricitynotifier

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.solvek.electricitynotifier.EnApp.Companion.enApp

class EnWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        TODO("Not yet implemented")
    }

    companion object {
        fun Context.schedulePeriodic(){

        }

        fun Context.handlePowerState(isOn: Boolean){
            enApp.log("Power state changed: $isOn")
        }
    }
}