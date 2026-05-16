package com.example.assignment_fit5046.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.assignment_fit5046.components.common.NotificationHelper
import com.example.assignment_fit5046.datamodels.AppNotification
import com.example.assignment_fit5046.services.local.AppDatabase
import com.example.assignment_fit5046.services.remote.firebase.NotificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DriveReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getStringExtra("alarmId") ?: return
        val driveId = intent.getStringExtra("driveId") ?: return
        val driveName = intent.getStringExtra("driveName") ?: return
        val recipientUid = intent.getStringExtra("recipientUid") ?: return
        val recipientRole = intent.getStringExtra("recipientRole") ?: return

        val title = "Drive Reminder"
        val message = "\"$driveName\" is happening tomorrow. Get ready to volunteer!"

        NotificationHelper.showNotification(
            context,
            title,
            message,
            NotificationHelper.CHANNEL_REMINDERS,
            alarmId.hashCode()
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                NotificationService.sendNotification(
                    AppNotification(
                        recipientUid = recipientUid,
                        recipientRole = recipientRole,
                        type = AppNotification.TYPE_DRIVE_REMINDER,
                        title = title,
                        message = message,
                        driveId = driveId,
                        driveName = driveName,
                        read = false,
                        createdAt = System.currentTimeMillis()
                    )
                )
                AppDatabase.getInstance(context).pendingAlarmDao().deleteAlarm(alarmId)
            } catch (_: Exception) {}
        }
    }
}
