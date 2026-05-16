package com.example.assignment_fit5046.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.assignment_fit5046.datamodels.PendingAlarm
import java.text.SimpleDateFormat
import java.util.Locale

object AlarmScheduler {

    fun schedule(context: Context, alarm: PendingAlarm) {
        if (alarm.triggerTimeMs <= System.currentTimeMillis()) return
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val intent = buildIntent(context, alarm)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.alarmId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarm.triggerTimeMs, pendingIntent)
        } catch (_: SecurityException) {
            // SCHEDULE_EXACT_ALARM not granted — skip silently
        }
    }

    fun cancel(context: Context, alarmId: String) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val intent = Intent(context, DriveReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }

    private fun buildIntent(context: Context, alarm: PendingAlarm): Intent =
        Intent(context, DriveReminderReceiver::class.java).apply {
            putExtra("alarmId", alarm.alarmId)
            putExtra("driveId", alarm.driveId)
            putExtra("driveName", alarm.driveName)
            putExtra("recipientUid", alarm.recipientUid)
            putExtra("recipientRole", alarm.recipientRole)
        }

    fun calculate24HrBeforeMs(dateStr: String): Long {
        val formats = listOf(
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        )
        for (sdf in formats) {
            try {
                val driveDate = sdf.parse(dateStr) ?: continue
                return driveDate.time - 24 * 60 * 60 * 1000L
            } catch (_: Exception) {}
        }
        return -1L
    }
}
