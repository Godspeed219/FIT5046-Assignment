package com.example.assignment_fit5046.screens.volunteer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.assignment_fit5046.components.common.AppToast
import com.example.assignment_fit5046.components.common.Screen
import com.example.assignment_fit5046.components.volunteer.WeatherCard
import com.example.assignment_fit5046.datamodels.ApplicationStatus
import com.example.assignment_fit5046.datamodels.UserRole
import com.example.assignment_fit5046.services.viewmodel.AuthState
import com.example.assignment_fit5046.services.viewmodel.AuthViewModel
import com.example.assignment_fit5046.services.viewmodel.MainViewModel
import com.example.assignment_fit5046.ui.StatusApproved
import com.example.assignment_fit5046.ui.StatusApprovedContainer
import com.example.assignment_fit5046.ui.StatusPending
import com.example.assignment_fit5046.ui.StatusPendingContainer
import com.example.assignment_fit5046.ui.StatusRejected
import com.example.assignment_fit5046.ui.StatusRejectedContainer
import com.example.assignment_fit5046.ui.SurfaceVariant
import com.example.assignment_fit5046.ui.TextDisabled

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriveDetailScreen(
    navController: NavController,
    driveId: String,
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel
) {
    val authState by authViewModel.authState.collectAsState()
    val currentUser = (authState as? AuthState.LoggedIn)?.user

    val allActiveDrives by mainViewModel.allActiveDrives.collectAsState()
    val volunteerApplications by mainViewModel.volunteerApplications.collectAsState()
    val errorMessage by mainViewModel.errorMessage.collectAsState()
    val successMessage by mainViewModel.successMessage.collectAsState()
    val driveWeather by mainViewModel.driveWeather.collectAsState()
    val driveDistance by mainViewModel.driveDistance.collectAsState()
    val unreadCount by mainViewModel.unreadCount.collectAsState()

    val drive = allActiveDrives.find { it.driveId == driveId }
    val existingApplication = volunteerApplications.find { it.driveId == driveId }

    var toastMessage by remember { mutableStateOf<String?>(null) }
    var showApplyDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var applyMessage by remember { mutableStateOf("") }

    LaunchedEffect(drive?.driveId) {
        drive?.let { mainViewModel.loadDriveWeatherAndDistance(it) }
    }

    DisposableEffect(Unit) {
        onDispose { mainViewModel.clearDriveWeather() }
    }

    LaunchedEffect(errorMessage, successMessage) {
        val msg = errorMessage ?: successMessage
        if (msg != null) {
            toastMessage = msg
            mainViewModel.clearMessages()
        }
    }

    if (showApplyDialog && drive != null) {
        AlertDialog(
            onDismissRequest = { showApplyDialog = false },
            title = { Text("Apply for ${drive.title}") },
            text = {
                Column {
                    Text(
                        "Add an optional message to your application:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = applyMessage,
                        onValueChange = { applyMessage = it },
                        placeholder = { Text("Why do you want to volunteer?") },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        currentUser?.let { user ->
                            mainViewModel.applyToDrive(
                                driveId = drive.driveId,
                                driveTitle = drive.title,
                                volunteerId = user.uid,
                                volunteerName = user.name,
                                message = applyMessage
                            )
                        }
                        showApplyDialog = false
                        applyMessage = ""
                    }
                ) { Text("Submit") }
            },
            dismissButton = {
                TextButton(onClick = { showApplyDialog = false; applyMessage = "" }) { Text("Cancel") }
            }
        )
    }

    if (showWithdrawDialog && drive != null) {
        AlertDialog(
            onDismissRequest = { showWithdrawDialog = false },
            title = { Text("Withdraw Application") },
            text = { Text("Are you sure you want to withdraw your application for \"${drive.title}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        existingApplication?.let {
                            mainViewModel.withdrawApplication(
                                applicationId = it.applicationId,
                                driveId = it.driveId
                            )
                        }
                        showWithdrawDialog = false
                    }
                ) { Text("Withdraw", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showWithdrawDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(drive?.title ?: "") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    BadgedBox(badge = { if (unreadCount > 0) Badge { Text("$unreadCount") } }) {
                        IconButton(onClick = { navController.navigate(Screen.Notifications.route) }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (drive != null) {
                Surface(shadowElevation = 8.dp, tonalElevation = 2.dp) {
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        when (val appStatus = existingApplication?.status) {
                            null, ApplicationStatus.WITHDRAWN -> {
                                Button(
                                    onClick = { showApplyDialog = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Apply Now") }
                            }
                            ApplicationStatus.PENDING -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    DriveStatusChip(status = appStatus, modifier = Modifier.weight(1f))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    OutlinedButton(
                                        onClick = { showWithdrawDialog = true },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        )
                                    ) { Text("Withdraw") }
                                }
                            }
                            else -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    DriveStatusChip(status = appStatus, modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (drive == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Drive not found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        if (drive.bannerUrl.isNotEmpty()) {
                            AsyncImage(
                                model = drive.bannerUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolunteerActivism,
                                    contentDescription = null,
                                    modifier = Modifier.size(72.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = drive.ngoName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            )
                            SuggestionChip(
                                onClick = {},
                                label = { Text(drive.category) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }

                    item {
                        Text(
                            text = drive.description,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }

                    item { HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp)) }

                    item {
                        Text(
                            text = "Event Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }

                    item {
                        DriveDetailRow(icon = Icons.Default.CalendarToday, text = drive.date)
                    }

                    if (drive.startTime.isNotBlank()) {
                        item {
                            DriveDetailRow(
                                icon = Icons.Default.AccessTime,
                                text = if (drive.endTime.isNotBlank())
                                    "${drive.startTime} – ${drive.endTime}"
                                else drive.startTime
                            )
                        }
                    }

                    item {
                        DriveDetailRow(icon = Icons.Default.LocationOn, text = drive.location)
                    }

                    item {
                        DriveDetailRow(
                            icon = Icons.Default.Group,
                            text = "${drive.maxVolunteers - drive.currentVolunteers} volunteer spots remaining"
                        )
                    }

                    driveDistance?.let { distKm ->
                        item {
                            DriveDetailRow(
                                icon = Icons.Default.DirectionsCar,
                                text = "%.1f km from Melbourne CBD".format(distKm)
                            )
                        }
                    }

                    driveWeather?.let { weather ->
                        item { WeatherCard(weather = weather) }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }

            AppToast(
                message = toastMessage ?: "",
                isVisible = toastMessage != null,
                role = UserRole.VOLUNTEER,
                onDismiss = { toastMessage = null }
            )
        }
    }
}

@Composable
private fun DriveDetailRow(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun DriveStatusChip(status: ApplicationStatus, modifier: Modifier = Modifier) {
    val (label, containerColor, contentColor) = when (status) {
        ApplicationStatus.PENDING -> Triple("Applied — Pending", StatusPendingContainer, StatusPending)
        ApplicationStatus.APPROVED -> Triple("Application Approved", StatusApprovedContainer, StatusApproved)
        ApplicationStatus.REJECTED -> Triple("Application Rejected", StatusRejectedContainer, StatusRejected)
        ApplicationStatus.WITHDRAWN -> Triple("Withdrawn", SurfaceVariant, TextDisabled)
    }
    AssistChip(
        onClick = {},
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = containerColor,
            labelColor = contentColor
        ),
        border = AssistChipDefaults.assistChipBorder(
            borderColor = contentColor.copy(alpha = 0.4f),
            enabled = true
        ),
        modifier = modifier
    )
}
