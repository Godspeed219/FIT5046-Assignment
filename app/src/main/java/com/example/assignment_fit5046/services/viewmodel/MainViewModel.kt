package com.example.assignment_fit5046.services.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.assignment_fit5046.datamodels.Application as VolunteerApplication
import com.example.assignment_fit5046.datamodels.ApplicationStatus
import com.example.assignment_fit5046.datamodels.Drive
import com.example.assignment_fit5046.datamodels.DriveStatus
import com.example.assignment_fit5046.services.local.AppDatabase
import com.example.assignment_fit5046.services.remote.firebase.ApplicationService
import com.example.assignment_fit5046.services.remote.firebase.DriveService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val driveDao = AppDatabase.getInstance(application).driveDao()
    private val applicationDao = AppDatabase.getInstance(application).applicationDao()

    private val _ngoDrives = MutableStateFlow<List<Drive>>(emptyList())
    val ngoDrives: StateFlow<List<Drive>> = _ngoDrives.asStateFlow()

    private val _ngoApplications = MutableStateFlow<List<VolunteerApplication>>(emptyList())
    val ngoApplications: StateFlow<List<VolunteerApplication>> = _ngoApplications.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun loadNgoDashboard(ngoId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val cached = driveDao.getAllDrives().filter { it.ngoId == ngoId }
                _ngoDrives.value = cached

                DriveService.getDrivesByNgo(ngoId)
                    .onSuccess { drives ->
                        driveDao.insertDrives(drives)
                        val fresh = drives.filter { it.ngoId == ngoId }
                        _ngoDrives.value = fresh

                        val allApplications = mutableListOf<VolunteerApplication>()
                        fresh.forEach { drive ->
                            ApplicationService.getApplicationsByDrive(drive.driveId)
                                .onSuccess { apps ->
                                    applicationDao.insertApplications(apps)
                                    allApplications.addAll(apps)
                                }
                                .onFailure { _errorMessage.value = it.message }
                        }
                        _ngoApplications.value = allApplications
                    }
                    .onFailure { _errorMessage.value = it.message }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createDrive(drive: Drive) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                DriveService.createDrive(drive)
                    .onSuccess { created ->
                        driveDao.insertDrive(created)
                        _ngoDrives.value = _ngoDrives.value + created
                        _successMessage.value = "Drive posted successfully"
                    }
                    .onFailure { _errorMessage.value = it.message }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateDriveStatus(driveId: String, status: DriveStatus) {
        viewModelScope.launch {
            DriveService.updateDriveStatus(driveId, status)
                .onSuccess {
                    _ngoDrives.value = _ngoDrives.value.map { drive ->
                        if (drive.driveId == driveId) drive.copy(status = status) else drive
                    }
                    _successMessage.value = "Drive status updated"
                }
                .onFailure { _errorMessage.value = it.message }
        }
    }

    fun deleteDrive(driveId: String) {
        viewModelScope.launch {
            DriveService.deleteDrive(driveId)
                .onSuccess {
                    val remaining = _ngoDrives.value.filter { it.driveId != driveId }
                    driveDao.clearDrives()
                    driveDao.insertDrives(remaining)
                    _ngoDrives.value = remaining
                    _successMessage.value = "Drive deleted"
                }
                .onFailure { _errorMessage.value = it.message }
        }
    }

    fun updateApplicationStatus(applicationId: String, status: ApplicationStatus, driveId: String) {
        viewModelScope.launch {
            ApplicationService.updateApplicationStatus(applicationId, status)
                .onSuccess {
                    _ngoApplications.value = _ngoApplications.value.map { app ->
                        if (app.applicationId == applicationId) app.copy(status = status) else app
                    }
                    _successMessage.value = when (status) {
                        ApplicationStatus.APPROVED -> "Applicant approved"
                        ApplicationStatus.REJECTED -> "Applicant rejected"
                        ApplicationStatus.PENDING -> "Application set to pending"
                    }
                }
                .onFailure { _errorMessage.value = it.message }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}
