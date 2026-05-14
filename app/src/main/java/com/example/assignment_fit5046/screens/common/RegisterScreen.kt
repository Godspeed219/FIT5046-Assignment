package com.example.assignment_fit5046.screens.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.assignment_fit5046.datamodels.UserRole
import com.example.assignment_fit5046.services.viewmodel.AuthState
import com.example.assignment_fit5046.services.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController, authViewModel: AuthViewModel) {
    var selectedRole by remember { mutableStateOf(UserRole.VOLUNTEER) }

    // Common fields
    var name by remember { mutableStateOf("") }
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

    // Inline validation
    val passwordMismatch = confirmPassword.isNotEmpty() && password != confirmPassword
    val passwordTooShort = password.isNotEmpty() && password.length < 6
    val canSubmit = !isLoading &&
            email.isNotBlank() &&
            password.length >= 6 &&
            password == confirmPassword &&
            (if (selectedRole == UserRole.VOLUNTEER) name.isNotBlank() else ngoName.isNotBlank())

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

        // Volunteer: Full Name
        AnimatedVisibility(
            visible = selectedRole == UserRole.VOLUNTEER,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; authViewModel.clearError() },
                    label = { Text("Full Name") },
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
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

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; authViewModel.clearError() },
            label = { Text("Email") },
            singleLine = true,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

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
            },
            enabled = canSubmit,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLoading) "Creating account..." else "Create Account")
        }

        Spacer(modifier = Modifier.height(12.dp))

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
    }
}
