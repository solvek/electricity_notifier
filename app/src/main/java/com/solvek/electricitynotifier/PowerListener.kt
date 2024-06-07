package com.solvek.electricitynotifier

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_POWER_CONNECTED
import com.solvek.electricitynotifier.EnWorker.Companion.handlePowerState


class PowerListener: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val isOn = intent.action == ACTION_POWER_CONNECTED
        context.handlePowerState(isOn)
    }
}