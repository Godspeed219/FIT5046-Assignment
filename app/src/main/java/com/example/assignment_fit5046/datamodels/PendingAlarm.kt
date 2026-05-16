package com.example.assignment_fit5046.datamodels

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_alarms")
data class PendingAlarm(
    @PrimaryKey val alarmId: String,
    val applicationId: String,
    val driveId: String,
    val driveName: String,
    val recipientUid: String,
    val recipientRole: String,
    val triggerTimeMs: Long,
    val type: String
) {
    companion object {
        const val TYPE_24HR = "REMINDER_24HR"
    }
}
