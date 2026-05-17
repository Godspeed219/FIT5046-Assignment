package com.example.assignment_fit5046

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.assignment_fit5046.components.common.AppNavigation
import com.example.assignment_fit5046.components.common.NotificationHelper
import com.example.assignment_fit5046.datamodels.UserRole
import com.example.assignment_fit5046.services.viewmodel.AuthState
import com.example.assignment_fit5046.services.viewmodel.AuthViewModel
import com.example.assignment_fit5046.services.viewmodel.MainViewModel
import com.example.assignment_fit5046.ui.AppTheme

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createNotificationChannels(this)
        enableEdgeToEdge()
        setContent {
            var currentRole by remember { mutableStateOf(UserRole.VOLUNTEER) }
            val authState by authViewModel.authState.collectAsState()

            var requestLocation by remember { mutableStateOf(false) }

            val locationPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { /* granted or denied — no action needed, fail silently */ }

            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) {
                // Notification dialog dismissed — signal LaunchedEffect to request location
                requestLocation = true
            }

            // Triggered by state change so Android safely accepts a new launch outside the result callback
            LaunchedEffect(requestLocation) {
                if (requestLocation) {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }

            LaunchedEffect(authState) {
                val state = authState
                if (state is AuthState.LoggedIn) {
                    authViewModel.registerFcmToken(state.user.uid)
                    mainViewModel.startNotificationListener(state.user.uid)
                    val prefs = applicationContext.getSharedPreferences("volunteerlink_prefs", Context.MODE_PRIVATE)
                    if (!prefs.getBoolean("permissions_requested", false)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            // On older APIs, no notification permission needed — go straight to location
                            requestLocation = true
                        }
                        prefs.edit().putBoolean("permissions_requested", true).apply()
                    }
                } else if (state is AuthState.LoggedOut) {
                    mainViewModel.stopNotificationListener()
                }
            }

            AppTheme(role = currentRole) {
                AppNavigation(
                    authViewModel = authViewModel,
                    onRoleChanged = { role -> currentRole = role }
                )
            }
        }
    }

}
