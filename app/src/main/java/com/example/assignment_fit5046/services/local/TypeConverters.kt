package com.example.assignment_fit5046.services.local

import androidx.room.TypeConverter
import com.example.assignment_fit5046.datamodels.ApplicationStatus
import com.example.assignment_fit5046.datamodels.DriveStatus
import com.example.assignment_fit5046.datamodels.UserRole

class Converters {
    @TypeConverter fun fromUserRole(role: UserRole): String = role.name
    @TypeConverter fun toUserRole(name: String): UserRole = UserRole.valueOf(name)

    @TypeConverter fun fromDriveStatus(status: DriveStatus): String = status.name
    @TypeConverter fun toDriveStatus(name: String): DriveStatus = DriveStatus.valueOf(name)

    @TypeConverter fun fromApplicationStatus(status: ApplicationStatus): String = status.name
    @TypeConverter fun toApplicationStatus(name: String): ApplicationStatus = ApplicationStatus.valueOf(name)
}
