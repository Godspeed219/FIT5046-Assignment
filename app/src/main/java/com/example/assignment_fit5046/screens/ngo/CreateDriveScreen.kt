package com.example.assignment_fit5046.screens.ngo

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.assignment_fit5046.components.common.AppLoader
import com.example.assignment_fit5046.components.common.AppToast
import com.example.assignment_fit5046.components.common.Screen
import com.example.assignment_fit5046.datamodels.Drive
import com.example.assignment_fit5046.datamodels.DriveStatus
import com.example.assignment_fit5046.datamodels.UserRole
import com.example.assignment_fit5046.services.viewmodel.AuthState
import com.example.assignment_fit5046.services.viewmodel.AuthViewModel
import com.example.assignment_fit5046.services.viewmodel.MainViewModel
import com.example.assignment_fit5046.ui.StatusApproved
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val CATEGORY_OPTIONS = listOf(
    "Environment", "Education", "Health", "Animal Welfare", "Community"
)

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDriveScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel
) {
    var currentStep by remember { mutableIntStateOf(1) }

    // Step 1
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    // Step 2
    var location by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var maxVolunteers by remember { mutableStateOf("") }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    // Step 3
    var bannerUri by remember { mutableStateOf<Uri?>(null) }
    var bannerUrl by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }

    val authState by authViewModel.authState.collectAsState()
    val currentUser = (authState as? AuthState.LoggedIn)?.user

    val isLoading by mainViewModel.isLoading.collectAsState()
    val successMessage by mainViewModel.successMessage.collectAsState()
    val errorMessage by mainViewModel.errorMessage.collectAsState()
    val unreadCount by mainViewModel.unreadCount.collectAsState()

    var toastMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> bannerUri = uri }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val startTimePickerState = rememberTimePickerState(is24Hour = true)
    val endTimePickerState = rememberTimePickerState(is24Hour = true)

    var categoryExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            mainViewModel.clearMessages()
            navController.navigate(Screen.DriveConfirmation.route) {
                popUpTo(Screen.CreateDrive.route) { inclusive = true }
            }
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
                title = { Text("Create Drive") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = Color.Black,
                    actionIconContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
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
                StepperHeader(currentStep = currentStep)

                when (currentStep) {
                    1 -> Step1Content(
                        title = title,
                        onTitleChange = { title = it },
                        description = description,
                        onDescriptionChange = { description = it },
                        category = category,
                        onCategoryChange = { category = it },
                        categoryExpanded = categoryExpanded,
                        onCategoryExpandedChange = { categoryExpanded = it },
                        onNext = { currentStep = 2 }
                    )
                    2 -> Step2Content(
                        location = location,
                        onLocationChange = { location = it },
                        date = date,
                        onPickDate = { showDatePicker = true },
                        startTime = startTime,
                        onPickStartTime = { showStartTimePicker = true },
                        endTime = endTime,
                        onPickEndTime = { showEndTimePicker = true },
                        maxVolunteers = maxVolunteers,
                        onMaxVolunteersChange = { maxVolunteers = it },
                        onBack = { currentStep = 1 },
                        onNext = { currentStep = 3 }
                    )
                    3 -> Step3Content(
                        bannerUri = bannerUri,
                        bannerUrl = bannerUrl,
                        isUploading = isUploading,
                        isLoading = isLoading,
                        onPickImage = { imagePickerLauncher.launch("image/*") },
                        onUpload = {
                            isUploading = true
                            mainViewModel.uploadDriveBanner(bannerUri!!, context) { url ->
                                bannerUrl = url ?: ""
                                isUploading = false
                            }
                        },
                        onBack = { currentStep = 2 },
                        onPost = { url ->
                            if (startTime.isBlank() || endTime.isBlank()) {
                                toastMessage = "Please select start and end time"
                            } else {
                                mainViewModel.createDrive(
                                    Drive(
                                        driveId = "",
                                        ngoId = currentUser?.uid ?: "",
                                        ngoName = currentUser?.ngoName ?: currentUser?.name ?: "",
                                        title = title,
                                        description = description,
                                        location = location,
                                        date = date,
                                        maxVolunteers = maxVolunteers.toIntOrNull() ?: 0,
                                        currentVolunteers = 0,
                                        category = category,
                                        status = DriveStatus.ACTIVE,
                                        createdAt = System.currentTimeMillis(),
                                        bannerUrl = url,
                                        startTime = startTime,
                                        endTime = endTime
                                    )
                                )
                            }
                        }
                    )
                }
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
private fun StepperHeader(currentStep: Int) {
    val stepLabels = listOf("Basic Info", "Details", "Banner")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        stepLabels.forEachIndexed { index, label ->
            val stepNum = index + 1
            val isCompleted = currentStep > stepNum
            val isActive = currentStep == stepNum

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(72.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = if (isCompleted || isActive) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = CircleShape
                        )
                        .then(
                            if (!isCompleted && !isActive)
                                Modifier.border(1.5.dp, MaterialTheme.colorScheme.onSurfaceVariant, CircleShape)
                            else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = "$stepNum",
                            color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isActive || isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            if (index < stepLabels.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 16.dp)
                ) {
                    HorizontalDivider(
                        color = if (currentStep > stepNum) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Step1Content(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    categoryExpanded: Boolean,
    onCategoryExpandedChange: (Boolean) -> Unit,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Drive Title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Description") },
            minLines = 4,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = onCategoryExpandedChange,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = if (category.isEmpty()) "" else category,
                onValueChange = {},
                readOnly = true,
                label = { Text("Category") },
                placeholder = { Text("Select category") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { onCategoryExpandedChange(false) }
            ) {
                CATEGORY_OPTIONS.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onCategoryChange(option)
                            onCategoryExpandedChange(false)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNext,
            enabled = title.isNotBlank() && description.isNotBlank() && category.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Next")
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun Step2Content(
    location: String,
    onLocationChange: (String) -> Unit,
    date: String,
    onPickDate: () -> Unit,
    startTime: String,
    onPickStartTime: () -> Unit,
    endTime: String,
    onPickEndTime: () -> Unit,
    maxVolunteers: String,
    onMaxVolunteersChange: (String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        OutlinedTextField(
            value = location,
            onValueChange = onLocationChange,
            label = { Text("Location / Address") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onPickDate,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (date.isEmpty()) "Pick Drive Date" else date)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.fillMaxWidth().clickable { onPickStartTime() }) {
            OutlinedTextField(
                value = startTime,
                onValueChange = {},
                enabled = false,
                label = { Text("Start Time *") },
                leadingIcon = {
                    Icon(Icons.Default.AccessTime, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.fillMaxWidth().clickable { onPickEndTime() }) {
            OutlinedTextField(
                value = endTime,
                onValueChange = {},
                enabled = false,
                label = { Text("End Time *") },
                leadingIcon = {
                    Icon(Icons.Default.AccessTime, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        val maxVolunteersError = maxVolunteers.isNotBlank() &&
            (maxVolunteers.toIntOrNull() == null || (maxVolunteers.toIntOrNull() ?: 0) < 1)

        OutlinedTextField(
            value = maxVolunteers,
            onValueChange = { if (it.all { c -> c.isDigit() }) onMaxVolunteersChange(it) },
            label = { Text("Max Volunteers") },
            singleLine = true,
            isError = maxVolunteersError,
            supportingText = if (maxVolunteersError) {
                { Text("Must be a number greater than 0") }
            } else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Back")
            }
            Button(
                onClick = onNext,
                enabled = location.isNotBlank() && date.isNotBlank() && (maxVolunteers.toIntOrNull() ?: 0) >= 1,
                modifier = Modifier.weight(1f)
            ) {
                Text("Next")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun Step3Content(
    bannerUri: Uri?,
    bannerUrl: String,
    isUploading: Boolean,
    isLoading: Boolean,
    onPickImage: () -> Unit,
    onUpload: () -> Unit,
    onBack: () -> Unit,
    onPost: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { onPickImage() },
            contentAlignment = Alignment.Center
        ) {
            if (bannerUri == null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap to add a banner image",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                AsyncImage(
                    model = bannerUri,
                    contentDescription = "Banner preview",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (bannerUri != null && bannerUrl.isEmpty()) {
            Button(
                onClick = onUpload,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isUploading
            ) {
                Text(if (isUploading) "Uploading..." else "Upload Banner")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (bannerUrl.isNotEmpty()) {
            Text(
                text = "Banner uploaded successfully",
                color = StatusApproved,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Back")
            }
            Button(
                onClick = { onPost(bannerUrl) },
                enabled = !isLoading && !(bannerUri != null && bannerUrl.isEmpty()),
                modifier = Modifier.weight(1f)
            ) {
                Text("Post Drive")
            }
        }

        if (bannerUri != null && bannerUrl.isEmpty()) {
            Text(
                text = "Please upload the banner before posting",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
