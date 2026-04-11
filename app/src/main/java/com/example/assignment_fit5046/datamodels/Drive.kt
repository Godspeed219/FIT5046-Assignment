package com.example.assignment_fit5046.datamodels

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drives")
data class Drive(
    @PrimaryKey val driveId: String = "",
    val ngoId: String = "",
    val ngoName: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val date: String = "",
    val maxVolunteers: Int = 0,
    val currentVolunteers: Int = 0,
    val category: String = "",
    val status: DriveStatus = DriveStatus.ACTIVE,
    val createdAt: Long = 0L
)

enum class DriveStatus {
    ACTIVE, CLOSED
}
