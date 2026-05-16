package com.example.assignment_fit5046.services.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.assignment_fit5046.datamodels.PendingAlarm

@Dao
interface PendingAlarmDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: PendingAlarm)

    @Query("DELETE FROM pending_alarms WHERE alarmId = :alarmId")
    suspend fun deleteAlarm(alarmId: String)

    @Query("SELECT * FROM pending_alarms")
    suspend fun getAllAlarms(): List<PendingAlarm>

    @Query("DELETE FROM pending_alarms WHERE driveId = :driveId")
    suspend fun deleteAlarmsForDrive(driveId: String)
}
