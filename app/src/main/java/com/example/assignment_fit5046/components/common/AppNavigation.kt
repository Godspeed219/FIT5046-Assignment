package com.example.assignment_fit5046.components.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.assignment_fit5046.components.ngo.NgoNavBar
import com.example.assignment_fit5046.components.volunteer.VolunteerNavBar
import com.example.assignment_fit5046.screens.common.LoginScreen
import com.example.assignment_fit5046.screens.common.RegisterScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object VolunteerHome : Screen("volunteer_home")
    object DriveDetail : Screen("drive_detail")
    object Search : Screen("search")
    object MyApplications : Screen("my_applications")
    object VolunteerProfile : Screen("volunteer_profile")
    object NgoDashboard : Screen("ngo_dashboard")
    object CreateDrive : Screen("create_drive")
    object ManageDrives : Screen("manage_drives")
    object NgoProfile : Screen("ngo_profile")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var currentRole by remember { mutableStateOf("") }

    Scaffold(
        bottomBar = {
            when (currentRole) {
                "VOLUNTEER" -> VolunteerNavBar(navController = navController)
                "NGO" -> NgoNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    navController = navController,
                    onRoleSet = { role -> currentRole = role }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    navController = navController,
                    onRoleSet = { role -> currentRole = role }
                )
            }
            composable(Screen.VolunteerHome.route) {
                Text("VolunteerHome Screen")
            }
            composable(Screen.DriveDetail.route) {
                Text("DriveDetail Screen")
            }
            composable(Screen.Search.route) {
                Text("Search Screen")
            }
            composable(Screen.MyApplications.route) {
                Text("MyApplications Screen")
            }
            composable(Screen.VolunteerProfile.route) {
                Text("VolunteerProfile Screen")
            }
            composable(Screen.NgoDashboard.route) {
                Text("NgoDashboard Screen")
            }
            composable(Screen.CreateDrive.route) {
                Text("CreateDrive Screen")
            }
            composable(Screen.ManageDrives.route) {
                Text("ManageDrives Screen")
            }
            composable(Screen.NgoProfile.route) {
                Text("NgoProfile Screen")
            }
        }
    }
}
