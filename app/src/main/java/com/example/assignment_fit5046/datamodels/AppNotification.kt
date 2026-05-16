package com.example.assignment_fit5046.datamodels

data class AppNotification(
    val notificationId: String = "",
    val recipientUid: String = "",
    val recipientRole: String = "",
    val type: String = "",
    val title: String = "",
    val message: String = "",
    val driveId: String = "",
    val driveName: String = "",
    val read: Boolean = false,
    val createdAt: Long = 0L
) {
    companion object {
        const val TYPE_APPLICATION_RECEIVED = "APPLICATION_RECEIVED"
        const val TYPE_APPLICATION_APPROVED = "APPLICATION_APPROVED"
        const val TYPE_APPLICATION_REJECTED = "APPLICATION_REJECTED"
        const val TYPE_APPLICATION_WITHDRAWN = "APPLICATION_WITHDRAWN"
        const val TYPE_DRIVE_CLOSED = "DRIVE_CLOSED"
        const val TYPE_DRIVE_REMINDER = "DRIVE_REMINDER"
        const val TYPE_DRIVE_CREATED = "DRIVE_CREATED"
    }
}
