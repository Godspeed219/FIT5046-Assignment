package com.example.assignment_fit5046.screens.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.assignment_fit5046.components.common.Screen

@Composable
fun RegisterScreen(navController: NavController, onRoleSet: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Volunteer") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
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
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            RadioButton(
                selected = selectedRole == "Volunteer",
                onClick = { selectedRole = "Volunteer" }
            )
            Text(
                text = "Volunteer",
                modifier = Modifier.clickable { selectedRole = "Volunteer" }
            )
            Spacer(modifier = Modifier.weight(1f))
            RadioButton(
                selected = selectedRole == "NGO",
                onClick = { selectedRole = "NGO" }
            )
            Text(
                text = "NGO",
                modifier = Modifier.clickable { selectedRole = "NGO" }
            )
            Spacer(modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                when (selectedRole) {
                    "Volunteer" -> {
                        onRoleSet("VOLUNTEER")
                        navController.navigate(Screen.VolunteerHome.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                    "NGO" -> {
                        onRoleSet("NGO")
                        navController.navigate(Screen.NgoDashboard.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Already have an account? Login",
            modifier = Modifier.clickable { navController.popBackStack() }
        )
    }
}
