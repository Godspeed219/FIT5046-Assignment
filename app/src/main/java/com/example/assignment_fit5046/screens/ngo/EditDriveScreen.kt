package com.example.assignment_fit5046.screens.ngo

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.assignment_fit5046.components.common.AppLoader
import com.example.assignment_fit5046.components.common.AppToast
import com.example.assignment_fit5046.datamodels.UserRole
import com.example.assignment_fit5046.services.viewmodel.MainViewModel
import com.example.assignment_fit5046.ui.StatusApproved
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDriveScreen(
    driveId: String,
    navController: NavController,
    mainViewModel: MainViewModel
) {
    val ngoDrives by mainViewModel.ngoDrives.collectAsState()
    val drive = ngoDrives.find { it.driveId == driveId }

    var title by remember(drive) { mutableStateOf(drive?.title ?: "") }
    var description by remember(drive) { mutableStateOf(drive?.description ?: "") }
    var location by remember(drive) { mutableStateOf(drive?.location ?: "") }
    var date by remember(drive) { mutableStateOf(drive?.date ?: "") }
    var startTime by remember(drive) { mutableStateOf(drive?.startTime ?: "") }
    var endTime by remember(drive) { mutableStateOf(drive?.endTime ?: "") }
    var maxVolunteers by remember(drive) { mutableStateOf(drive?.maxVolunteers?.toString() ?: "") }
    var bannerUri by remember { mutableStateOf<Uri?>(null) }
    var bannerUrl by remember(drive) { mutableStateOf(drive?.bannerUrl ?: "") }
    var isUploading by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val isLoading by mainViewModel.isLoading.collectAsState()
    val successMessage by mainViewModel.successMessage.collectAsState()
    val errorMessage by mainViewModel.errorMessage.collectAsState()

    var toastMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val datePickerState = rememberDatePickerState()
    val startTimePickerState = rememberTimePickerState(is24Hour = true)
    val endTimePickerState = rememberTimePickerState(is24Hour = true)

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> bannerUri = uri }

    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            mainViewModel.clearMessages()
            navController.popBackStack()
        }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            toastMessage = errorMessage
            mainViewModel.clearMessages()
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showStartTimePicker) {
        AlertDialog(
            onDismissRequest = { showStartTimePicker = false },
            title = { Text("Select Start Time") },
            text = { TimePicker(state = startTimePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    startTime = String.format("%02d:%02d", startTimePickerState.hour, startTimePickerState.minute)
                    showStartTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimePicker = false }) { Text("Cancel") }
            }
        )
    }

    if (showEndTimePicker) {
        AlertDialog(
            onDismissRequest = { showEndTimePicker = false },
            title = { Text("Select End Time") },
            text = { TimePicker(state = endTimePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    endTime = String.format("%02d:%02d", endTimePickerState.hour, endTimePickerState.minute)
                    showEndTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndTimePicker = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Drive") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, tonalElevation = 2.dp) {
                Button(
                    onClick = {
                        drive?.let {
                            mainViewModel.updateDrive(
                                it.copy(
                                    title = title,
                                    description = description,
                                    location = location,
                                    date = date,
                                    startTime = startTime,
                                    endTime = endTime,
                                    maxVolunteers = maxVolunteers.toIntOrNull() ?: it.maxVolunteers,
                                    bannerUrl = bannerUrl
                                )
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    enabled = !isLoading && !isUploading
                ) {
                    Text("Save Changes")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Banner hero — full width, tappable
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        bannerUri != null -> {
                            AsyncImage(
                                model = bannerUri,
                                contentDescription = "Banner preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        bannerUrl.isNotEmpty() -> {
                            AsyncImage(
                                model = bannerUrl,
                                contentDescription = "Current banner",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        else -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Tap to add banner",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    // Edit overlay icon
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Change",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Upload button + status
                if (bannerUri != null && bannerUrl == (drive?.bannerUrl ?: "")) {
                    Button(
                        onClick = {
                            isUploading = true
                            mainViewModel.uploadDriveBanner(bannerUri!!, context) { url ->
                                bannerUrl = url ?: bannerUrl
                                isUploading = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        enabled = !isUploading
                    ) {
                        Text(if (isUploading) "Uploading..." else "Upload New Banner")
                    }
                }

                if (bannerUrl != (drive?.bannerUrl ?: "") && bannerUrl.isNotEmpty()) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = StatusApproved
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "New banner uploaded",
                            color = StatusApproved,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Section: Drive Details
                SectionHeader("Drive Details")

                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        minLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location") },
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Section: Schedule
                SectionHeader("Schedule")

                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    // Date field — read-only, opens DatePicker on click
                    Box(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }) {
                        OutlinedTextField(
                            value = date,
                            onValueChange = {},
                            enabled = false,
                            label = { Text("Drive Date") },
                            leadingIcon = {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showStartTimePicker = true }
                        ) {
                            OutlinedTextField(
                                value = startTime,
                                onValueChange = {},
                                enabled = false,
                                label = { Text("Start Time") },
                                leadingIcon = {
                                    Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(18.dp))
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showEndTimePicker = true }
                        ) {
                            OutlinedTextField(
                                value = endTime,
                                onValueChange = {},
                                enabled = false,
                                label = { Text("End Time") },
                                leadingIcon = {
                                    Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(18.dp))
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = maxVolunteers,
                        onValueChange = { maxVolunteers = it },
                        label = { Text("Max Volunteers") },
                        leadingIcon = {
                            Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            AppLoader(
                isLoading = isLoading || isUploading,
                role = UserRole.NGO
            )

            AppToast(
                message = toastMessage ?: "",
                isVisible = toastMessage != null,
                role = UserRole.NGO,
                onDismiss = { toastMessage = null }
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Column {
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}
