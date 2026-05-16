package com.example.assignment_fit5046.components.common

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.assignment_fit5046.MainActivity

object NotificationHelper {

    const val CHANNEL_APPLICATIONS = "applications"
    const val CHANNEL_DRIVES = "drives"
    const val CHANNEL_REMINDERS = "reminders"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_APPLICATIONS, "Applications", NotificationManager.IMPORTANCE_DEFAULT)
                    .apply { description = "Application status updates" }
            )
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_DRIVES, "Drives", NotificationManager.IMPORTANCE_DEFAULT)
                    .apply { description = "Drive updates and closures" }
            )
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_REMINDERS, "Reminders", NotificationManager.IMPORTANCE_HIGH)
                    .apply { description = "24-hour drive reminders" }
            )
        }
    }

    fun showNotification(context: Context, title: String, message: String, channelId: String, notificationId: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS permission not granted — skip silently
        }
    }
}
