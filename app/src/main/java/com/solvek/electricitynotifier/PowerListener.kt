package com.solvek.electricitynotifier

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_POWER_CONNECTED
import android.util.Log
import com.solvek.electricitynotifier.EnApp.Companion.enApp


class PowerListener: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = with(context.enApp){
        Log.d("PowerListener", "Power event received: ${intent.action}")
        if (intent.action == ACTION_POWER_CONNECTED){
            electricityOn()
        }
        else {
            electricityOff()
        }
    }
}