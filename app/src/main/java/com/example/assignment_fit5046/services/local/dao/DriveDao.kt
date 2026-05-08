package com.example.assignment_fit5046.services.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.assignment_fit5046.datamodels.Drive

@Dao
interface DriveDao {
    @Query("SELECT * FROM drives ORDER BY createdAt DESC")
    suspend fun getAllDrives(): List<Drive>

    @Query("SELECT * FROM drives WHERE driveId = :driveId")
    suspend fun getDriveById(driveId: String): Drive?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrives(drives: List<Drive>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrive(drive: Drive)

    @Query("DELETE FROM drives")
    suspend fun clearDrives()
}
