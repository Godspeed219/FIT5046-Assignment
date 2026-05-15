package com.example.assignment_fit5046.screens.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.assignment_fit5046.R
import com.example.assignment_fit5046.datamodels.UserRole
import com.example.assignment_fit5046.services.viewmodel.AuthState
import com.example.assignment_fit5046.services.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController, authViewModel: AuthViewModel) {
    val context = LocalContext.current
    val webClientId = stringResource(R.string.default_web_client_id)

    val pendingGoogleUser by authViewModel.pendingGoogleUser.collectAsState()
    val isGoogleMode = pendingGoogleUser != null

    var selectedRole by remember { mutableStateOf(UserRole.VOLUNTEER) }

    // Common fields
    var name by remember { mutableStateOf(pendingGoogleUser?.name ?: "") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    // Volunteer-only
    var bio by remember { mutableStateOf("") }

    // NGO-only
    var ngoName by remember { mutableStateOf("") }
    var ngoDescription by remember { mutableStateOf("") }

    val authState by authViewModel.authState.collectAsState()
    val isLoading = authState is AuthState.Loading
    val errorMessage = (authState as? AuthState.Error)?.message

    // Sync name when pendingGoogleUser arrives (e.g., after CredentialManager resolves)
    LaunchedEffect(pendingGoogleUser) {
        if (pendingGoogleUser != null && name.isEmpty()) {
            name = pendingGoogleUser!!.name
        }
    }

    // Inline validation
    val passwordMismatch = confirmPassword.isNotEmpty() && password != confirmPassword
    val passwordTooShort = password.isNotEmpty() && password.length < 6
    val canSubmit = if (isGoogleMode) {
        !isLoading
    } else {
        !isLoading &&
            email.isNotBlank() &&
            password.length >= 6 &&
            password == confirmPassword &&
            (if (selectedRole == UserRole.VOLUNTEER) name.isNotBlank() else ngoName.isNotBlank())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "VolunteerLink",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Create your account",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Role selector
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = selectedRole == UserRole.VOLUNTEER,
                onClick = { selectedRole = UserRole.VOLUNTEER },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) {
                Text("Volunteer")
            }
            SegmentedButton(
                selected = selectedRole == UserRole.NGO,
                onClick = { selectedRole = UserRole.NGO },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) {
                Text("NGO")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Volunteer: Full Name (read-only and always visible in Google mode)
        AnimatedVisibility(
            visible = selectedRole == UserRole.VOLUNTEER || isGoogleMode,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (!isGoogleMode) { name = it; authViewModel.clearError() } },
                    label = { Text("Full Name") },
                    singleLine = true,
                    enabled = !isLoading && !isGoogleMode,
                    readOnly = isGoogleMode,
                    modifier = Modifier.fillMaxWidth()
                )
                if (isGoogleMode && pendingGoogleUser != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Signed in as ${pendingGoogleUser!!.email}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // NGO: Organisation Name
        AnimatedVisibility(
            visible = selectedRole == UserRole.NGO,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column {
                OutlinedTextField(
                    value = ngoName,
                    onValueChange = { ngoName = it; authViewModel.clearError() },
                    label = { Text("Organisation Name") },
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        if (!isGoogleMode) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; authViewModel.clearError() },
                label = { Text("Email") },
                singleLine = true,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number (optional)") },
            singleLine = true,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Volunteer: Bio
        AnimatedVisibility(
            visible = selectedRole == UserRole.VOLUNTEER,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column {
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio (optional)") },
                    minLines = 2,
                    maxLines = 3,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // NGO: Description
        AnimatedVisibility(
            visible = selectedRole == UserRole.NGO,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column {
                OutlinedTextField(
                    value = ngoDescription,
                    onValueChange = { ngoDescription = it },
                    label = { Text("Mission / Description (optional)") },
                    minLines = 2,
                    maxLines = 3,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        if (!isGoogleMode) {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; authViewModel.clearError() },
                label = { Text("Password") },
                singleLine = true,
                enabled = !isLoading,
                isError = passwordTooShort,
                supportingText = if (passwordTooShort) {
                    { Text("At least 6 characters") }
                } else null,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; authViewModel.clearError() },
                label = { Text("Confirm Password") },
                singleLine = true,
                enabled = !isLoading,
                isError = passwordMismatch,
                supportingText = if (passwordMismatch) {
                    { Text("Passwords do not match") }
                } else null,
                visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { confirmVisible = !confirmVisible }) {
                        Icon(
                            imageVector = if (confirmVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
        } // end if (!isGoogleMode)

        Spacer(modifier = Modifier.height(8.dp))

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                if (isGoogleMode) {
                    authViewModel.completeGoogleRegistration(selectedRole)
                } else {
                    authViewModel.register(
                        email = email.trim(),
                        password = password,
                        name = if (selectedRole == UserRole.VOLUNTEER) name.trim() else ngoName.trim(),
                        role = selectedRole,
                        phoneNumber = phone.trim(),
                        bio = bio.trim(),
                        ngoName = ngoName.trim(),
                        ngoDescription = ngoDescription.trim()
                    )
                }
            },
            enabled = canSubmit,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                when {
                    isLoading -> "Please wait..."
                    isGoogleMode -> "Continue with Google"
                    else -> "Create Account"
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isGoogleMode) {
            TextButton(
                onClick = {
                    authViewModel.clearPendingGoogleUser()
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Use a different account",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Already have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Login",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { navController.popBackStack() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = "or",
                    modifier = Modifier.padding(horizontal = 12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { authViewModel.signInWithGoogle(context, webClientId) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.google_signin),
                    contentDescription = "Google",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Continue with Google", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
