package com.solvek.electricitynotifier

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import com.solvek.electricitynotifier.EnWorker.Companion.syncPowerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

class EnApp: Application() {
    private val _log = MutableStateFlow("")
    val log = _log.asStateFlow()

    private lateinit var prefs: SharedPreferences

    lateinit var record: Record
        private set
    var isOn: Boolean = true
        private set

    private val _enabled = MutableStateFlow(true)
    val enabled = _enabled.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences("EnApp", 0)

        record = Record(
            prefs.getLong(KEY_RECORD_TIME, 0L),
            prefs.getBoolean(KEY_RECORD_IS_ON, true)
        )
        isOn = prefs.getBoolean(KEY_IS_ON, true)
        _enabled.value = prefs.getBoolean(KEY_ENABLED, true)

        registerPowerListener()
    }

    fun registerAction(time: Long, isOn: Boolean){
        prefs.edit()
            .putLong(KEY_RECORD_TIME, time)
            .putBoolean(KEY_RECORD_IS_ON, isOn)
            .apply()
        record = Record(time, isOn)
    }

    fun log(message: String){
        val c = _log.value
        val prefix = if (c.length > 5000){
            ""
        }
        else {
            c+"\r\n"
        }
        _log.value = "$prefix${Date()}: $message"
    }

    fun toggleAvailability(){
        val toEnable = !_enabled.value
        if (toEnable){
            syncPowerState()
        }
        prefs.edit()
            .putBoolean(KEY_ENABLED, toEnable)
            .apply()
        _enabled.value = toEnable
    }

    fun electricityOn() {
        setCurrentStatus(true)
        log("Electricity ON")
        syncPowerState()
    }

    fun electricityOff(){
        setCurrentStatus(false)
        log("Electricity OFF")
        syncPowerState()
    }
    private fun setCurrentStatus(currentIsOn: Boolean) {
        prefs.edit()
            .putBoolean(KEY_IS_ON, currentIsOn)
            .apply()
        isOn = currentIsOn
    }

    private fun registerPowerListener(){
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        registerReceiver(PowerListener(), filter)
    }

    companion object {
        val Context.enApp get() = this.applicationContext as EnApp

        private const val KEY_RECORD_TIME = "Record_time"
        private const val KEY_RECORD_IS_ON = "Record_is_on"
        private const val KEY_IS_ON = "Is_on"
        private const val KEY_ENABLED = "Enabled"
    }
}