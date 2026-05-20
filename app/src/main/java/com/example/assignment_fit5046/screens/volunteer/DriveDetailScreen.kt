package com.example.assignment_fit5046.screens.volunteer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.assignment_fit5046.components.common.AppLoader
import com.example.assignment_fit5046.components.common.AppToast
import com.example.assignment_fit5046.components.common.Screen
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
import java.time.LocalTime
import java.time.format.DateTimeFormatter

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
    val ngoDrives by mainViewModel.ngoDrives.collectAsState()
    val volunteerApplications by mainViewModel.volunteerApplications.collectAsState()
    val errorMessage by mainViewModel.errorMessage.collectAsState()
    val successMessage by mainViewModel.successMessage.collectAsState()
    val driveWeather by mainViewModel.driveWeather.collectAsState()
    val driveDistance by mainViewModel.driveDistance.collectAsState()
    val unreadCount by mainViewModel.unreadCount.collectAsState()

    val drive = allActiveDrives.find { it.driveId == driveId } ?: ngoDrives.find { it.driveId == driveId }
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
                title = {},
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
            if (drive != null && currentUser?.role != UserRole.NGO) {
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

                    // Item 1 — Hero banner (220dp)
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        ) {
                            if (drive.bannerUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(drive.bannerUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolunteerActivism,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            // Category pill — bottom left
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(start = 14.dp, bottom = 14.dp)
                                    .background(Color(0x99000000), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                            ) {
                                Text(drive.category, color = Color.White, fontSize = 12.sp)
                            }
                            // Spots left pill — bottom right
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 14.dp, bottom = 14.dp)
                                    .background(Color(0x99000000), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "${drive.maxVolunteers - drive.currentVolunteers} spots left",
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // Item 2 — 3-column stat strip
                    item {
                        val timeValue = if (drive.startTime.isNotBlank()) {
                            try {
                                val parsePattern = DateTimeFormatter.ofPattern("H:mm")
                                val displayPattern = DateTimeFormatter.ofPattern("h:mm a")
                                val startFormatted = LocalTime.parse(drive.startTime, parsePattern).format(displayPattern)
                                if (drive.endTime.isNotBlank()) {
                                    val endFormatted = LocalTime.parse(drive.endTime, parsePattern).format(displayPattern)
                                    "$startFormatted – $endFormatted"
                                } else startFormatted
                            } catch (_: Exception) {
                                if (drive.endTime.isNotBlank()) "${drive.startTime} – ${drive.endTime}" else drive.startTime
                            }
                        } else "–"

                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatColumn(
                                    modifier = Modifier.weight(1f),
                                    icon = {
                                        Icon(
                                            Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    label = "Date",
                                    value = drive.date
                                )
                                Box(
                                    modifier = Modifier
                                        .width(0.5.dp)
                                        .height(40.dp)
                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                )
                                StatColumn(
                                    modifier = Modifier.weight(1f),
                                    icon = {
                                        Icon(
                                            Icons.Default.AccessTime,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    label = "Time",
                                    value = timeValue
                                )
                                Box(
                                    modifier = Modifier
                                        .width(0.5.dp)
                                        .height(40.dp)
                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                )
                                StatColumn(
                                    modifier = Modifier.weight(1f),
                                    icon = {
                                        Icon(
                                            Icons.Default.Group,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    label = "Spots",
                                    value = "${drive.maxVolunteers - drive.currentVolunteers} left"
                                )
                            }
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            )
                        }
                    }

                    // Item 3 — NGO name + description
                    item {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Text(
                                text = drive.ngoName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = drive.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Item 4 — Info rows (location + optional distance)
                    item {
                        InfoRow(
                            icon = {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            text = drive.location
                        )
                    }

                    driveDistance?.let { distKm ->
                        item {
                            InfoRow(
                                icon = {
                                    Icon(
                                        Icons.Default.DirectionsCar,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                text = "%.1f km from Melbourne CBD".format(distKm)
                            )
                        }
                    }

                    // Item 5 — Weather card
                    driveWeather?.let { weather ->
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(top = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Temperature + condition label
                                    Column {
                                        Text(
                                            text = "${weather.current.temperature2m.toInt()}°",
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight(500),
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Text(
                                            text = weather.current.description,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                        )
                                    }
                                    // Vertical divider
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 12.dp)
                                            .width(0.5.dp)
                                            .height(40.dp)
                                            .background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f))
                                    )
                                    // Wind + Condition columns
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                "Wind",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                            )
                                            Text(
                                                "${weather.current.windSpeed10m.toInt()} km/h",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                "Condition",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                            )
                                            Text(
                                                weather.current.description,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Item 6 — Bottom spacer
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }

            // Full-screen loader until weather data arrives
            AppLoader(
                isLoading = driveWeather == null && drive != null,
                role = UserRole.VOLUNTEER
            )

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
private fun StatColumn(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    label: String,
    value: String
) {
    Column(
        modifier = modifier.padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        icon()
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun InfoRow(
    icon: @Composable () -> Unit,
    text: String
) {
    Column {
        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = text, style = MaterialTheme.typography.bodyMedium)
        }
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
