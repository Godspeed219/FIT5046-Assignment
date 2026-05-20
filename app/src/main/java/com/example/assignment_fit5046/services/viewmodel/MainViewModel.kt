package com.example.assignment_fit5046.services.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.assignment_fit5046.BuildConfig
import com.example.assignment_fit5046.datamodels.AppNotification
import retrofit2.HttpException
import com.example.assignment_fit5046.datamodels.Application as VolunteerApplication
import com.example.assignment_fit5046.datamodels.ApplicationStatus
import com.example.assignment_fit5046.datamodels.Drive
import com.example.assignment_fit5046.datamodels.DriveStatus
import com.example.assignment_fit5046.datamodels.GlobalGivingProject
import com.example.assignment_fit5046.datamodels.PendingAlarm
import com.example.assignment_fit5046.datamodels.Quote
import com.example.assignment_fit5046.datamodels.User
import com.example.assignment_fit5046.datamodels.UserRole
import com.example.assignment_fit5046.datamodels.WeatherResponse
import com.example.assignment_fit5046.components.common.NotificationHelper
import com.example.assignment_fit5046.services.AlarmScheduler
import com.example.assignment_fit5046.services.ContextEngine
import com.example.assignment_fit5046.services.LocationSimulator
import com.example.assignment_fit5046.services.local.AppDatabase
import com.example.assignment_fit5046.services.remote.RetrofitClient
import com.example.assignment_fit5046.services.remote.firebase.ApplicationService
import com.example.assignment_fit5046.services.remote.firebase.DriveService
import com.example.assignment_fit5046.services.remote.firebase.NotificationService
import com.example.assignment_fit5046.services.remote.firebase.UserService
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val driveDao = AppDatabase.getInstance(application).driveDao()
    private val applicationDao = AppDatabase.getInstance(application).applicationDao()
    private val userDao = AppDatabase.getInstance(application).userDao()
    private val pendingAlarmDao = AppDatabase.getInstance(application).pendingAlarmDao()

    private var notificationListener: ListenerRegistration? = null

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

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

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _allActiveDrives = MutableStateFlow<List<Drive>>(emptyList())
    val allActiveDrives: StateFlow<List<Drive>> = _allActiveDrives.asStateFlow()

    private val _quote = MutableStateFlow<Quote?>(null)
    val quote: StateFlow<Quote?> = _quote.asStateFlow()

    private val _volunteerApplications = MutableStateFlow<List<VolunteerApplication>>(emptyList())
    val volunteerApplications: StateFlow<List<VolunteerApplication>> = _volunteerApplications.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Drive>>(emptyList())
    val searchResults: StateFlow<List<Drive>> = _searchResults.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _profileUpdateSuccess = MutableStateFlow(false)
    val profileUpdateSuccess: StateFlow<Boolean> = _profileUpdateSuccess.asStateFlow()

    private val _ngoSearchResults = MutableStateFlow<List<GlobalGivingProject>>(emptyList())
    val ngoSearchResults: StateFlow<List<GlobalGivingProject>> = _ngoSearchResults.asStateFlow()

    private val _driveWeather = MutableStateFlow<WeatherResponse?>(null)
    val driveWeather: StateFlow<WeatherResponse?> = _driveWeather.asStateFlow()

    private val _driveDistance = MutableStateFlow<Double?>(null)
    val driveDistance: StateFlow<Double?> = _driveDistance.asStateFlow()

    private val _ngoModalResults = MutableStateFlow<List<GlobalGivingProject>>(emptyList())
    val ngoModalResults: StateFlow<List<GlobalGivingProject>> = _ngoModalResults.asStateFlow()

    private val _ngoModalLoading = MutableStateFlow(false)
    val ngoModalLoading: StateFlow<Boolean> = _ngoModalLoading.asStateFlow()

    private val _ngoModalError = MutableStateFlow<String?>(null)
    val ngoModalError: StateFlow<String?> = _ngoModalError.asStateFlow()

    private val _driveCoordinates = mutableMapOf<String, Pair<Double, Double>>()

    private val _driveCoordinatesState = MutableStateFlow<Map<String, Pair<Double, Double>>>(emptyMap())
    val driveCoordinatesState: StateFlow<Map<String, Pair<Double, Double>>> = _driveCoordinatesState

    private val _contextRankedDrives = MutableStateFlow<List<Drive>>(emptyList())
    val contextRankedDrives: StateFlow<List<Drive>> = _contextRankedDrives

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

    fun closeDrive(driveId: String) {
        viewModelScope.launch {
            DriveService.updateDriveStatus(driveId, DriveStatus.CLOSED)
                .onSuccess {
                    _ngoDrives.value = _ngoDrives.value.map { drive ->
                        if (drive.driveId == driveId) drive.copy(status = DriveStatus.CLOSED) else drive
                    }
                    val closedDrive = _ngoDrives.value.find { it.driveId == driveId }
                    closedDrive?.let { driveDao.insertDrive(it) }
                    _successMessage.value = "Drive closed"

                    val driveName = closedDrive?.title ?: driveId
                    val affectedApps = _ngoApplications.value.filter {
                        it.driveId == driveId &&
                            (it.status == ApplicationStatus.PENDING || it.status == ApplicationStatus.APPROVED)
                    }
                    affectedApps.forEach { app ->
                        sendNotification(
                            AppNotification(
                                recipientUid = app.volunteerId,
                                recipientRole = UserRole.VOLUNTEER.name,
                                type = AppNotification.TYPE_DRIVE_CLOSED,
                                title = "Drive Closed",
                                message = "\"$driveName\" has been closed by the organiser.",
                                driveId = driveId,
                                driveName = driveName,
                                read = false,
                                createdAt = System.currentTimeMillis()
                            )
                        )
                        val alarmId = "drive_${driveId}_${app.volunteerId}"
                        launch(Dispatchers.IO) {
                            try {
                                AlarmScheduler.cancel(getApplication(), alarmId)
                                pendingAlarmDao.deleteAlarm(alarmId)
                            } catch (_: Exception) {}
                        }
                    }
                }
                .onFailure { _errorMessage.value = it.message; Log.e("FAILURE QUERY", _errorMessage.value.toString()) }
        }
    }

    fun expirePassedDrives(ngoId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val today = sdf.parse(sdf.format(Date())) ?: return@launch

                DriveService.getDrivesByNgo(ngoId)
                    .onSuccess { drives ->
                        drives.filter { it.status == DriveStatus.ACTIVE }.forEach { drive ->
                            val driveDate = try { sdf.parse(drive.date) } catch (e: Exception) { null }
                                ?: return@forEach
                            if (driveDate.before(today)) {
                                DriveService.updateDriveStatus(drive.driveId, DriveStatus.CLOSED)
                                    .onSuccess { driveDao.insertDrive(drive.copy(status = DriveStatus.CLOSED)) }
                                    .onFailure { Log.e("EXPIRE_DRIVES", it.message.toString()) }
                            }
                        }
                        val refreshed = driveDao.getAllDrives().filter { it.ngoId == ngoId }
                        _ngoDrives.value = refreshed
                    }
                    .onFailure { Log.e("EXPIRE_DRIVES", it.message.toString()) }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateApplicationStatus(applicationId: String, status: ApplicationStatus, driveId: String) {
        viewModelScope.launch {
            ApplicationService.updateApplicationStatus(applicationId, status)
                .onSuccess {
                    // Capture previous status before StateFlow is updated
                    val previousStatus = _ngoApplications.value.find { it.applicationId == applicationId }?.status

                    _ngoApplications.value = _ngoApplications.value.map { app ->
                        if (app.applicationId == applicationId) app.copy(status = status) else app
                    }
                    _successMessage.value = when (status) {
                        ApplicationStatus.APPROVED -> "Applicant approved"
                        ApplicationStatus.REJECTED -> "Applicant rejected"
                        ApplicationStatus.PENDING -> "Application set to pending"
                        else -> {""}
                    } as String?

                    val app = _ngoApplications.value.find { it.applicationId == applicationId }
                    val drive = _ngoDrives.value.find { it.driveId == driveId }
                    if (app != null && drive != null) {
                        // Notify volunteer of approval or rejection
                        if (status == ApplicationStatus.APPROVED || status == ApplicationStatus.REJECTED) {
                            sendNotification(
                                AppNotification(
                                    recipientUid = app.volunteerId,
                                    recipientRole = UserRole.VOLUNTEER.name,
                                    type = if (status == ApplicationStatus.APPROVED)
                                        AppNotification.TYPE_APPLICATION_APPROVED
                                    else
                                        AppNotification.TYPE_APPLICATION_REJECTED,
                                    title = if (status == ApplicationStatus.APPROVED)
                                        "Application Approved"
                                    else
                                        "Application Rejected",
                                    message = if (status == ApplicationStatus.APPROVED)
                                        "Your application for \"${drive.title}\" has been approved!"
                                    else
                                        "Your application for \"${drive.title}\" has been rejected.",
                                    driveId = driveId,
                                    driveName = drive.title,
                                    read = false,
                                    createdAt = System.currentTimeMillis()
                                )
                            )
                        }

                        // Schedule 24hr AlarmManager reminder when application is approved
                        if (status == ApplicationStatus.APPROVED) {
                            val triggerMs = AlarmScheduler.calculate24HrBeforeMs(drive.date)
                            if (triggerMs > System.currentTimeMillis()) {
                                val alarm = PendingAlarm(
                                    alarmId = UUID.randomUUID().toString(),
                                    applicationId = applicationId,
                                    driveId = driveId,
                                    driveName = drive.title,
                                    recipientUid = app.volunteerId,
                                    recipientRole = UserRole.VOLUNTEER.name,
                                    triggerTimeMs = triggerMs,
                                    type = PendingAlarm.TYPE_24HR
                                )
                                pendingAlarmDao.insertAlarm(alarm)
                                AlarmScheduler.schedule(getApplication(), alarm)
                            }
                        }

                        // Cancel alarm if rejected
                        if (status == ApplicationStatus.REJECTED) {
                            AlarmScheduler.cancel(getApplication(), applicationId)
                            pendingAlarmDao.deleteAlarmsByApplication(applicationId)
                        }

                        // Sync Room with updated status
                        applicationDao.updateStatus(applicationId, status)
                    }

                    // Update volunteer spot count based on status change
                    when (status) {
                        ApplicationStatus.APPROVED -> {
                            // Increment spots taken when approved
                            DriveService.incrementVolunteerCount(driveId)
                            _allActiveDrives.value = _allActiveDrives.value.map { d ->
                                if (d.driveId == driveId) d.copy(currentVolunteers = d.currentVolunteers + 1) else d
                            }
                            _ngoDrives.value = _ngoDrives.value.map { d ->
                                if (d.driveId == driveId) d.copy(currentVolunteers = d.currentVolunteers + 1) else d
                            }
                            driveDao.insertDrive(
                                _ngoDrives.value.find { it.driveId == driveId } ?: return@onSuccess
                            )
                        }
                        ApplicationStatus.REJECTED -> {
                            // Find if this application was previously approved — if so decrement
                            if (previousStatus == ApplicationStatus.APPROVED) {
                                DriveService.decrementVolunteerCount(driveId)
                                _allActiveDrives.value = _allActiveDrives.value.map { d ->
                                    if (d.driveId == driveId) d.copy(
                                        currentVolunteers = maxOf(0, d.currentVolunteers - 1)
                                    ) else d
                                }
                                _ngoDrives.value = _ngoDrives.value.map { d ->
                                    if (d.driveId == driveId) d.copy(
                                        currentVolunteers = maxOf(0, d.currentVolunteers - 1)
                                    ) else d
                                }
                                driveDao.insertDrive(
                                    _ngoDrives.value.find { it.driveId == driveId } ?: return@onSuccess
                                )
                            }
                        }
                        else -> { /* PENDING and WITHDRAWN do not affect spot count */ }
                    }
                }
                .onFailure { _errorMessage.value = it.message }
        }
    }

    fun uploadDriveBanner(imageUri: Uri, context: Context, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                Log.d("CLOUDINARY", "Starting upload for URI: $imageUri")
                val secureUrl = withContext(Dispatchers.IO) {
                    val bytes = context.contentResolver.openInputStream(imageUri)?.readBytes()
                        ?: return@withContext null

                    val boundary = "Boundary_${System.currentTimeMillis()}"
                    val conn = URL("https://api.cloudinary.com/v1_1/dhdbdnvd3/image/upload")
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

                    Log.d("CLOUDINARY", "Response code: ${conn.responseCode}")
                    if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                        val response = conn.inputStream.bufferedReader().readText()
                        Log.d("CLOUDINARY", "Response body: $response")
                        JSONObject(response).getString("secure_url")
                    } else {
                        null
                    }
                }
                if (secureUrl != null) Log.d("CLOUDINARY", "Upload success: $secureUrl")
                if (secureUrl == null) _errorMessage.value = "Failed to upload image"
                onResult(secureUrl)
            } catch (e: Exception) {
                Log.e("CLOUDINARY", "Upload exception: ${e::class.simpleName} — ${e.message}")
                _errorMessage.value = e.message
                onResult(null)
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
                    .onFailure { _errorMessage.value = it.message; Log.e(
                        "FAILURE QUERY",
                        _errorMessage.value.toString(),
                    ) }

            // Geocode each drive for context ranking
            _allActiveDrives.value.forEach { drive ->
                if (!_driveCoordinates.containsKey(drive.driveId)) {
                    try {
                        val results = RetrofitClient.geocodingApi.geocode(drive.location)
                        val geo = results.firstOrNull()
                        if (geo != null) {
                            val lat = geo.lat.toDoubleOrNull()
                            val lon = geo.lon.toDoubleOrNull()
                            if (lat != null && lon != null) {
                                _driveCoordinates[drive.driveId] = Pair(lat, lon)
                                _driveCoordinatesState.value = _driveCoordinates.toMap()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("ContextEngine", "Geocoding failed for ${drive.location}: ${e.message}")
                    }
                }
            }
            updateContextRanking()

            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshVolunteerHome(volunteerId: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                DriveService.getAllActiveDrives()
                    .onSuccess { drives ->
                        driveDao.insertDrives(drives)
                        _allActiveDrives.value = drives
                    }
                    .onFailure { _errorMessage.value = it.message ?: "Failed to load drives"; Log.e(
                        "FAILURE QUERY",
                        _errorMessage.value.toString(),
                    ) }

                ApplicationService.getApplicationsByVolunteer(volunteerId)
                    .onSuccess { apps ->
                        applicationDao.insertApplications(apps)
                        _volunteerApplications.value = apps
                    }
                    .onFailure { _errorMessage.value = it.message; Log.e(
                        "FAILURE QUERY",
                        _errorMessage.value.toString(),
                    ) }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun refreshNgoDashboard(ngoId: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
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
                                .onFailure { _errorMessage.value = it.message; Log.e("FAILURE QUERY", _errorMessage.value.toString()) }
                        }
                        _ngoApplications.value = allApplications
                    }
                    .onFailure { _errorMessage.value = it.message; Log.e("FAILURE QUERY", _errorMessage.value.toString()) }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun refreshDriveApplications(driveId: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                ApplicationService.getApplicationsByDrive(driveId)
                    .onSuccess { apps ->
                        applicationDao.insertApplications(apps)
                        val updated = _ngoApplications.value.filter { it.driveId != driveId } + apps
                        _ngoApplications.value = updated
                    }
                    .onFailure { _errorMessage.value = it.message; Log.e("FAILURE QUERY", _errorMessage.value.toString()) }
            } finally {
                _isRefreshing.value = false
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
                        val fresh = userDao.getUser()
                        if (fresh != null) {
                            _updatedUser.value = fresh
                            _currentUser.value = fresh
                        }
                        _successMessage.value = "Profile updated successfully"
                        _profileUpdateSuccess.value = true
                    }
                    .onFailure { _errorMessage.value = it.message ?: "Failed to update profile"; Log.e(
                        "FAILURE QUERY",
                        _errorMessage.value.toString(),
                    ) }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadDriveApplications(driveId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                ApplicationService.getApplicationsByDrive(driveId)
                    .onSuccess { apps ->
                        applicationDao.insertApplications(apps)
                        val updated = _ngoApplications.value.filter { it.driveId != driveId } + apps
                        _ngoApplications.value = updated
                    }
                    .onFailure { _errorMessage.value = it.message; Log.e("FAILURE QUERY", _errorMessage.value.toString()) }
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

    fun searchGlobalGivingByCategory(category: String) {
        val themeId = when (category) {
            "Education"     -> "edu"
            "Health"        -> "health"
            "Environment"   -> "climate"
            "Animal Welfare"-> "animals"
            "Community"     -> "rights"
            else            -> { _ngoSearchResults.value = emptyList(); return }
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.globalGivingApi.getProjectsByTheme(
                    theme = themeId,
                    apiKey = BuildConfig.GLOBAL_GIVING_API_KEY
                )
                _ngoSearchResults.value = response.projects.project
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load NGO projects: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun withdrawApplication(applicationId: String, driveId: String) {
        viewModelScope.launch {
            ApplicationService.updateApplicationStatus(applicationId, ApplicationStatus.WITHDRAWN)
                .onSuccess {
                    val app = _volunteerApplications.value.find { it.applicationId == applicationId }
                    _volunteerApplications.value = _volunteerApplications.value.map { a ->
                        if (a.applicationId == applicationId) a.copy(status = ApplicationStatus.WITHDRAWN) else a
                    }
                    _successMessage.value = "Application withdrawn"

                    val drive = _allActiveDrives.value.find { it.driveId == driveId }
                    if (app != null && drive != null) {
                        sendNotification(
                            AppNotification(
                                recipientUid = drive.ngoId,
                                recipientRole = UserRole.NGO.name,
                                type = AppNotification.TYPE_APPLICATION_WITHDRAWN,
                                title = "Application Withdrawn",
                                message = "${app.volunteerName} withdrew their application for \"${drive.title}\"",
                                driveId = driveId,
                                driveName = drive.title,
                                read = false,
                                createdAt = System.currentTimeMillis()
                            )
                        )
                    }
                    // Cancel any scheduled reminder for this volunteer + drive
                    val alarmId = "drive_${driveId}_${app?.volunteerId ?: ""}"
                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            AlarmScheduler.cancel(getApplication(), alarmId)
                            pendingAlarmDao.deleteAlarm(alarmId)
                        } catch (_: Exception) {}
                    }
                }
                .onFailure { _errorMessage.value = it.message; Log.e(
                    "FAILURE QUERY",
                    _errorMessage.value.toString(),
                ) }
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
                        _volunteerApplications.value += application
                        _successMessage.value = "Application submitted successfully"

                        // Notify NGO of new application
                        val drive = _allActiveDrives.value.find { it.driveId == driveId }
                        if (drive != null) {
                            sendNotification(
                                AppNotification(
                                    recipientUid = drive.ngoId,
                                    recipientRole = UserRole.NGO.name,
                                    type = AppNotification.TYPE_APPLICATION_RECEIVED,
                                    title = "New Application",
                                    message = "$volunteerName applied for \"$driveTitle\"",
                                    driveId = driveId,
                                    driveName = driveTitle,
                                    read = false,
                                    createdAt = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                    .onFailure { _errorMessage.value = it.message ?: "Failed to apply" }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshDrives(query: String = "", category: String = "All") {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                DriveService.getAllActiveDrives()
                    .onSuccess { drives ->
                        driveDao.insertDrives(drives)
                        _allActiveDrives.value = drives
                        _searchResults.value = drives.filter { drive ->
                            (category == "All" || drive.category == category) &&
                                    (query.isEmpty() || drive.title.contains(query, ignoreCase = true) ||
                                            drive.description.contains(query, ignoreCase = true))
                        }
                    }
                    .onFailure { _errorMessage.value = it.message; Log.e("FAILURE QUERY", it.message.toString()) }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun refreshMyApplications(volunteerId: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                ApplicationService.getApplicationsByVolunteer(volunteerId)
                    .onSuccess { apps ->
                        applicationDao.insertApplications(apps)
                        _volunteerApplications.value = apps
                    }
                    .onFailure { _errorMessage.value = it.message; Log.e("FAILURE QUERY", it.message.toString()) }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun refreshCurrentUser(uid: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                UserService.getCurrentUser()
                    .onSuccess { user ->
                        userDao.insertUser(user)
                        _currentUser.value = user
                    }
                    .onFailure { _errorMessage.value = it.message; Log.e("FAILURE QUERY", it.message.toString()) }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun refreshNgoDrives(ngoId: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                DriveService.getDrivesByNgo(ngoId)
                    .onSuccess { drives ->
                        driveDao.insertDrives(drives)
                        _ngoDrives.value = drives.filter { it.ngoId == ngoId }
                    }
                    .onFailure { _errorMessage.value = it.message; Log.e("FAILURE QUERY", it.message.toString()) }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun refreshApplicationsForDrive(driveId: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                ApplicationService.getApplicationsByDrive(driveId)
                    .onSuccess { apps ->
                        applicationDao.insertApplications(apps)
                        val updated = _ngoApplications.value.filter { it.driveId != driveId } + apps
                        _ngoApplications.value = updated
                    }
                    .onFailure { _errorMessage.value = it.message; Log.e("FAILURE QUERY", it.message.toString()) }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun loadDriveWeatherAndDistance(drive: Drive) {
        viewModelScope.launch {
            try {
                val results = RetrofitClient.geocodingApi.geocode(drive.location)
                val geo = results.firstOrNull() ?: return@launch
                val lat = geo.lat.toDoubleOrNull() ?: return@launch
                val lon = geo.lon.toDoubleOrNull() ?: return@launch
                _driveWeather.value = RetrofitClient.weatherApi.getWeather(lat, lon)
                _driveDistance.value = haversineKm(-37.8136, 144.9631, lat, lon)
            } catch (_: Exception) {}
        }
    }

    fun clearDriveWeather() {
        _driveWeather.value = null
        _driveDistance.value = null
    }

    fun searchNgoModal(keyword: String) {
        if (keyword.isBlank()) return
        viewModelScope.launch {
            _ngoModalLoading.value = true
            _ngoModalError.value = null
            try {
                val response = RetrofitClient.globalGivingApi.searchProjects(
                    apiKey = BuildConfig.GLOBAL_GIVING_API_KEY,
                    keyword = keyword
                )
                val projects = response.search?.response?.projects?.project ?: emptyList()
                Log.d("NGO_SEARCH", "Raw projects returned: ${projects.size}")
                // Deduplicate by org name — show unique organisations only
                val uniqueOrgs = projects
                    .filter { it.organization?.name != null }
                    .distinctBy { it.organization?.name }
                    .take(15)
                Log.d("NGO_SEARCH", "Unique orgs: ${uniqueOrgs.size}")
                _ngoModalResults.value = uniqueOrgs
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("NGO_SEARCH", "HTTP ${e.code()} — $errorBody")
                _ngoModalError.value = "Search failed (${e.code()}). Try a different keyword."
            } catch (e: Exception) {
                Log.e("NGO_SEARCH", "Error: ${e.message}")
                _ngoModalError.value = e.message
            } finally {
                _ngoModalLoading.value = false
            }
        }
    }

    fun clearNgoModal() {
        _ngoModalResults.value = emptyList()
        _ngoModalLoading.value = false
        _ngoModalError.value = null
    }

    fun updateContextRanking() {
        val location = LocationSimulator.getCurrentLocation()
        val drives = _allActiveDrives.value
        val applications = _volunteerApplications.value
        Log.d("ContextEngine", "updateContextRanking called — ${drives.size} drives, location=$location")
        _contextRankedDrives.value = ContextEngine.rankDrives(
            drives = drives,
            location = location,
            applications = applications,
            driveCoordinates = _driveCoordinates
        )
    }

    private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    fun clearProfileUpdateSuccess() {
        _profileUpdateSuccess.value = false
    }

    fun refreshQuote() {
        viewModelScope.launch {
            repeat(2) { attempt ->
                try {
                    val result = RetrofitClient.quotableApi.getRandomQuote().firstOrNull()
                    if (result != null && result.content.isNotBlank()) {
                        _quote.value = result
                        return@launch
                    }
                } catch (e: Exception) {
                    if (attempt == 1) {
                        _quote.value = Quote(
                            id = "static",
                            content = "The best way to find yourself is to lose yourself in the service of others.",
                            author = "Mahatma Gandhi",
                            tags = emptyList(),
                            length = 86
                        )
                    }
                }
            }
        }
    }

    fun startNotificationListener(uid: String) {
        stopNotificationListener()
        var isFirstLoad = true
        var knownIds = emptySet<String>()
        notificationListener = NotificationService.listenForNotifications(uid) { notifications ->
            val currentIds = notifications.map { it.notificationId }.toSet()

            if (!isFirstLoad) {
                notifications.filter { it.notificationId !in knownIds }.forEach { n ->
                    val channelId = when (n.type) {
                        AppNotification.TYPE_APPLICATION_RECEIVED,
                        AppNotification.TYPE_APPLICATION_APPROVED,
                        AppNotification.TYPE_APPLICATION_REJECTED,
                        AppNotification.TYPE_APPLICATION_WITHDRAWN -> NotificationHelper.CHANNEL_APPLICATIONS
                        AppNotification.TYPE_DRIVE_REMINDER -> NotificationHelper.CHANNEL_REMINDERS
                        else -> NotificationHelper.CHANNEL_DRIVES
                    }
                    NotificationHelper.showNotification(
                        context = getApplication(),
                        title = n.title,
                        message = n.message,
                        channelId = channelId,
                        notificationId = n.notificationId.hashCode()
                    )
                }
            }

            isFirstLoad = false
            knownIds = currentIds
            _notifications.value = notifications
            _unreadCount.value = notifications.count { !it.read }
        }
    }

    fun stopNotificationListener() {
        notificationListener?.remove()
        notificationListener = null
    }

    fun markNotificationRead(notificationId: String) {
        viewModelScope.launch {
            NotificationService.markAsRead(notificationId)
            _notifications.value = _notifications.value.map { n ->
                if (n.notificationId == notificationId) n.copy(read = true) else n
            }
            _unreadCount.value = _notifications.value.count { !it.read }
        }
    }

    private suspend fun sendNotification(notification: AppNotification) {
        try {
            NotificationService.sendNotification(notification)
        } catch (_: Exception) {}
    }

    private fun scheduleReminderAlarm(context: Context, drive: Drive, volunteerId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val triggerMs = AlarmScheduler.calculate24HrBeforeMs(drive.date)
                if (triggerMs <= System.currentTimeMillis()) return@launch
                val alarmId = "drive_${drive.driveId}_$volunteerId"
                val alarm = PendingAlarm(
                    alarmId = alarmId,
                    applicationId = "",
                    driveId = drive.driveId,
                    driveName = drive.title,
                    recipientUid = volunteerId,
                    recipientRole = UserRole.VOLUNTEER.name,
                    triggerTimeMs = triggerMs,
                    type = PendingAlarm.TYPE_24HR
                )
                pendingAlarmDao.insertAlarm(alarm)
                AlarmScheduler.schedule(context, alarm)
            } catch (_: Exception) {}
        }
    }
}
