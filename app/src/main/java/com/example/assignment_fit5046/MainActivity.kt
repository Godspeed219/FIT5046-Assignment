package com.example.assignment_fit5046

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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

            LaunchedEffect(authState) {
                val state = authState
                if (state is AuthState.LoggedIn) {
                    authViewModel.registerFcmToken(state.user.uid)
                    mainViewModel.startNotificationListener(state.user.uid)
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
