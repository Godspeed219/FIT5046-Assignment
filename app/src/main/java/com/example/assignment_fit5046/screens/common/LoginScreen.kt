package com.example.assignment_fit5046.screens.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.assignment_fit5046.components.common.Screen

@Composable
fun LoginScreen(navController: NavController, onRoleSet: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = Color.Red)
            Spacer(modifier = Modifier.height(8.dp))
        }
        Button(
            onClick = {
                when {
                    email == "volunteer@test.com" && password == "password123" -> {
                        onRoleSet("VOLUNTEER")
                        navController.navigate(Screen.VolunteerHome.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                    email == "ngo@test.com" && password == "password123" -> {
                        onRoleSet("NGO")
                        navController.navigate(Screen.NgoDashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                    else -> errorMessage = "Invalid credentials"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Don't have an account? Register",
            modifier = Modifier.clickable { navController.navigate(Screen.Register.route) }
        )
    }
}
