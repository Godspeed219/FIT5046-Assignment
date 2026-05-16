package com.example.assignment_fit5046.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.assignment_fit5046.services.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val alarms = AppDatabase.getInstance(context).pendingAlarmDao().getAllAlarms()
                alarms.forEach { alarm ->
                    if (alarm.triggerTimeMs > System.currentTimeMillis()) {
                        AlarmScheduler.schedule(context, alarm)
                    } else {
                        AppDatabase.getInstance(context).pendingAlarmDao().deleteAlarm(alarm.alarmId)
                    }
                }
            } catch (_: Exception) {}
        }
    }
}
