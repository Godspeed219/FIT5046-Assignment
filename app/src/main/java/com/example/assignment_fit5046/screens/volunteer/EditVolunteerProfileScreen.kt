package com.example.assignment_fit5046.screens.volunteer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.assignment_fit5046.components.common.AppLoader
import com.example.assignment_fit5046.components.common.AppToast
import com.example.assignment_fit5046.datamodels.UserRole
import com.example.assignment_fit5046.services.viewmodel.AuthState
import com.example.assignment_fit5046.services.viewmodel.AuthViewModel
import com.example.assignment_fit5046.services.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditVolunteerProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel
) {
    val authState by authViewModel.authState.collectAsState()
    val currentUser = (authState as? AuthState.LoggedIn)?.user

    var name by remember(currentUser) { mutableStateOf(currentUser?.name ?: "") }
    var bio by remember(currentUser) { mutableStateOf(currentUser?.bio ?: "") }
    var phoneNumber by remember(currentUser) { mutableStateOf(currentUser?.phoneNumber ?: "") }
    var pendingSaveUser by remember { mutableStateOf<com.example.assignment_fit5046.datamodels.User?>(null) }

    val isLoading by mainViewModel.isLoading.collectAsState()
    val profileUpdateSuccess by mainViewModel.profileUpdateSuccess.collectAsState()
    val errorMessage by mainViewModel.errorMessage.collectAsState()

    var toastMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(profileUpdateSuccess) {
        if (profileUpdateSuccess) {
            pendingSaveUser?.let { authViewModel.updateCurrentUser(it) }
            pendingSaveUser = null
            toastMessage = "Profile updated successfully"
            mainViewModel.clearMessages()
            mainViewModel.clearProfileUpdateSuccess()
            navController.popBackStack()
        }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            toastMessage = errorMessage
            mainViewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = Color.Black,
                    actionIconContentColor = MaterialTheme.colorScheme.primary,
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
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
                    .padding(20.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(96.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Profile photo coming soon",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = currentUser?.email ?: "",
                    onValueChange = {},
                    label = { Text("Email") },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = currentUser?.role?.name ?: "",
                    onValueChange = {},
                    label = { Text("Role") },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))

                val phoneError = phoneNumber.isNotBlank() &&
                    !phoneNumber.matches(Regex("^[+]?[0-9]{8,15}$"))

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { if (it.length <= 15) phoneNumber = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = phoneError,
                    supportingText = if (phoneError) {
                        { Text("Enter a valid phone number (8-15 digits)") }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && name.isNotBlank() && !phoneError,
                    onClick = {
                        val updatedUser = currentUser!!.copy(
                            name = name,
                            bio = bio,
                            phoneNumber = phoneNumber
                        )
                        pendingSaveUser = updatedUser
                        mainViewModel.saveUserProfile(updatedUser)
                    }
                ) {
                    Text("Save Changes")
                }
            }

            AppLoader(
                isLoading = isLoading,
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
