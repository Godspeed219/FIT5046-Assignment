package com.example.assignment_fit5046

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.assignment_fit5046.components.common.AppNavigation
import com.example.assignment_fit5046.datamodels.UserRole
import com.example.assignment_fit5046.services.viewmodel.AuthViewModel
import com.example.assignment_fit5046.ui.AppTheme

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var currentRole by remember { mutableStateOf(UserRole.VOLUNTEER) }
            AppTheme(role = currentRole) {
                AppNavigation(
                    authViewModel = authViewModel,
                    onRoleChanged = { role -> currentRole = role }
                )
            }
        }
    }
}
