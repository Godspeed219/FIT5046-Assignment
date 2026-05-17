package com.example.assignment_fit5046.services.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.assignment_fit5046.datamodels.Application
import com.example.assignment_fit5046.datamodels.ApplicationStatus

@Dao
interface ApplicationDao {
    @Query("SELECT * FROM applications WHERE volunteerId = :volunteerId ORDER BY appliedAt DESC")
    suspend fun getApplicationsByVolunteer(volunteerId: String): List<Application>

    @Query("SELECT * FROM applications WHERE driveId = :driveId ORDER BY appliedAt DESC")
    suspend fun getApplicationsByDrive(driveId: String): List<Application>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplications(applications: List<Application>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplication(application: Application)

    @Query("DELETE FROM applications WHERE applicationId = :applicationId")
    suspend fun deleteApplication(applicationId: String)

    @Query("DELETE FROM applications")
    suspend fun clearApplications()

    @Query("UPDATE applications SET status = :status WHERE applicationId = :applicationId")
    suspend fun updateStatus(applicationId: String, status: ApplicationStatus)
}
