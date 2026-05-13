package com.example.assignment_fit5046.services.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.assignment_fit5046.datamodels.Application as VolunteerApplication
import com.example.assignment_fit5046.datamodels.ApplicationStatus
import com.example.assignment_fit5046.datamodels.Drive
import com.example.assignment_fit5046.datamodels.DriveStatus
import com.example.assignment_fit5046.datamodels.Quote
import com.example.assignment_fit5046.datamodels.User
import com.example.assignment_fit5046.services.local.AppDatabase
import com.example.assignment_fit5046.services.remote.firebase.ApplicationService
import com.example.assignment_fit5046.services.remote.firebase.DriveService
import com.example.assignment_fit5046.services.remote.firebase.UserService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.HttpURLConnection
import java.net.URL

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val driveDao = AppDatabase.getInstance(application).driveDao()
    private val applicationDao = AppDatabase.getInstance(application).applicationDao()
    private val userDao = AppDatabase.getInstance(application).userDao()

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

    private val _updatedUser = MutableStateFlow<User?>(null)
    val updatedUser: StateFlow<User?> = _updatedUser.asStateFlow()

    private val _allActiveDrives = MutableStateFlow<List<Drive>>(emptyList())
    val allActiveDrives: StateFlow<List<Drive>> = _allActiveDrives.asStateFlow()

    private val _quote = MutableStateFlow<Quote?>(null)
    val quote: StateFlow<Quote?> = _quote.asStateFlow()

    private val _volunteerApplications = MutableStateFlow<List<VolunteerApplication>>(emptyList())
    val volunteerApplications: StateFlow<List<VolunteerApplication>> = _volunteerApplications.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Drive>>(emptyList())
    val searchResults: StateFlow<List<Drive>> = _searchResults.asStateFlow()

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
                                .onFailure { _errorMessage.value = it.message; Log.e(
                                    "FAILURE QUERY",
                                    _errorMessage.value.toString(),
                                ) }
                        }
                        _ngoApplications.value = allApplications
                    }
                    .onFailure { _errorMessage.value = it.message; Log.e(
                        "FAILURE QUERY",
                        _errorMessage.value.toString(),
                    ) }
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
                        _ngoDrives.value += created
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
                        else -> {""}
                    } as String?
                }
                .onFailure { _errorMessage.value = it.message }
        }
    }

    fun uploadDriveBanner(imageUri: Uri, context: Context, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val secureUrl = withContext(Dispatchers.IO) {
                    val bytes = context.contentResolver.openInputStream(imageUri)?.readBytes()
                        ?: return@withContext null

                    val boundary = "Boundary_${System.currentTimeMillis()}"
                    val conn = URL("https://api.cloudinary.com/v1_1/dhdbdnvd3ly/image/upload")
                        .openConnection() as HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.doOutput = true
                    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")

                    val out = conn.outputStream
                    val writer = PrintWriter(OutputStreamWriter(out, "UTF-8"), true)

                    writer.append("--$boundary\r\n")
                    writer.append("Content-Disposition: form-data; name=\"upload_preset\"\r\n\r\n")
                    writer.append("VolunteerLink\r\n")
                    writer.flush()

                    writer.append("--$boundary\r\n")
                    writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"banner.jpg\"\r\n")
                    writer.append("Content-Type: application/octet-stream\r\n\r\n")
                    writer.flush()
                    out.write(bytes)
                    out.flush()
                    writer.append("\r\n")
                    writer.append("--$boundary--\r\n")
                    writer.flush()

                    if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                        val response = conn.inputStream.bufferedReader().readText()
                        JSONObject(response).getString("secure_url")
                    } else {
                        null
                    }
                }
                if (secureUrl == null) _errorMessage.value = "Failed to upload image"
                onResult(secureUrl)
            } catch (e: Exception) {
                _errorMessage.value = e.message
                onResult(null)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateDrive(drive: Drive) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                DriveService.updateDrive(drive)
                    .onSuccess {
                        _ngoDrives.value = _ngoDrives.value.map { d ->
                            if (d.driveId == drive.driveId) drive else d
                        }
                        driveDao.insertDrive(drive)
                        _successMessage.value = "Drive updated successfully"
                    }
                    .onFailure { _errorMessage.value = it.message }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadVolunteerHome(volunteerId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val cached = driveDao.getAllDrives().filter { it.status == DriveStatus.ACTIVE }
                _allActiveDrives.value = cached

                DriveService.getAllActiveDrives()
                    .onSuccess { drives ->
                        driveDao.insertDrives(drives)
                        _allActiveDrives.value = drives
                        _searchResults.value = drives
                    }
                    .onFailure { _errorMessage.value = it.message ?: "Failed to load drives"; Log.e("FAILURE QUERY", _errorMessage.value.toString()) }

                ApplicationService.getApplicationsByVolunteer(volunteerId)
                    .onSuccess { apps ->
                        applicationDao.insertApplications(apps)
                        _volunteerApplications.value = apps
                    }
                    .onFailure { _errorMessage.value = it.message }

                _quote.value = Quote(
                    id = "static",
                    content = "The best way to find yourself is to lose yourself in the service of others.",
                    author = "Mahatma Gandhi",
                    tags = emptyList(),
                    length = 86
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshVolunteerHome(volunteerId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                DriveService.getAllActiveDrives()
                    .onSuccess { drives ->
                        driveDao.insertDrives(drives)
                        _allActiveDrives.value = drives
                    }
                    .onFailure { _errorMessage.value = it.message ?: "Failed to load drives" }

                ApplicationService.getApplicationsByVolunteer(volunteerId)
                    .onSuccess { apps ->
                        applicationDao.insertApplications(apps)
                        _volunteerApplications.value = apps
                    }
                    .onFailure { _errorMessage.value = it.message }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveUserProfile(user: User) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                UserService.updateUser(user)
                    .onSuccess {
                        userDao.insertUser(user)
                        _updatedUser.value = user
                        _successMessage.value = "Profile updated successfully"
                    }
                    .onFailure { _errorMessage.value = it.message ?: "Failed to update profile" }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchDrives(query: String, category: String) {
        val base = _allActiveDrives.value
        _searchResults.value = base.filter { drive ->
            (category == "All" || drive.category == category) &&
                (query.isEmpty() || drive.title.contains(query, ignoreCase = true) ||
                    drive.description.contains(query, ignoreCase = true))
        }
    }

    fun withdrawApplication(applicationId: String, driveId: String) {
        viewModelScope.launch {
            ApplicationService.updateApplicationStatus(applicationId, ApplicationStatus.WITHDRAWN)
                .onSuccess {
                    _volunteerApplications.value = _volunteerApplications.value.map { app ->
                        if (app.applicationId == applicationId) app.copy(status = ApplicationStatus.WITHDRAWN) else app
                    }
                    _successMessage.value = "Application withdrawn"
                }
                .onFailure { _errorMessage.value = it.message }
        }
    }

    fun applyToDrive(
        driveId: String,
        driveTitle: String,
        volunteerId: String,
        volunteerName: String,
        message: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                ApplicationService.applyToDrive(driveId, driveTitle, volunteerId, volunteerName, message)
                    .onSuccess { application ->
                        applicationDao.insertApplication(application)
                        _volunteerApplications.value = _volunteerApplications.value + application
                        _successMessage.value = "Application submitted successfully"
                    }
                    .onFailure { _errorMessage.value = it.message ?: "Failed to apply" }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}
