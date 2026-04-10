package com.example.volunteerlink_fit5046.datamodels

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "applications")
data class Application(
    @PrimaryKey val applicationId: String = "",
    val driveId: String = "",
    val driveTitle: String = "",
    val volunteerId: String = "",
    val volunteerName: String = "",
    val status: ApplicationStatus = ApplicationStatus.PENDING,
    val appliedAt: Long = 0L,
    val message: String = ""
)

enum class ApplicationStatus {
    PENDING, APPROVED, REJECTED
}
