package com.example.assignment_fit5046.components.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.assignment_fit5046.components.ngo.NgoNavBar
import com.example.assignment_fit5046.components.volunteer.VolunteerNavBar
import com.example.assignment_fit5046.datamodels.UserRole
import com.example.assignment_fit5046.screens.common.LoginScreen
import com.example.assignment_fit5046.screens.common.RegisterScreen
import com.example.assignment_fit5046.screens.ngo.CreateDriveScreen
import com.example.assignment_fit5046.screens.ngo.DriveApplicationsScreen
import com.example.assignment_fit5046.screens.ngo.ManageDrivesScreen
import com.example.assignment_fit5046.screens.ngo.NgoDashboardScreen
import com.example.assignment_fit5046.screens.ngo.NgoProfileScreen
import com.example.assignment_fit5046.screens.volunteer.DriveDetailScreen
import com.example.assignment_fit5046.screens.volunteer.HomeScreen
import com.example.assignment_fit5046.screens.volunteer.MyApplicationsScreen
import com.example.assignment_fit5046.screens.volunteer.ProfileScreen
import com.example.assignment_fit5046.screens.volunteer.SearchScreen
import com.example.assignment_fit5046.services.viewmodel.AuthState
import com.example.assignment_fit5046.services.viewmodel.AuthViewModel

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
    object NgoApplications : Screen("ngo_applications")
}

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    onRoleChanged: (UserRole) -> Unit = {}
) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsState()
    var currentRole by remember { mutableStateOf<UserRole?>(null) }

    // Central auth-driven navigation
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.LoggedIn -> {
                currentRole = state.user.role
                onRoleChanged(state.user.role)
                val dest = if (state.user.role == UserRole.VOLUNTEER)
                    Screen.VolunteerHome.route else Screen.NgoDashboard.route
                navController.navigate(dest) {
                    popUpTo(0) { inclusive = true }
                }
            }
            is AuthState.LoggedOut -> {
                currentRole = null
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
            else -> {}
        }
    }

    val onSignOut: () -> Unit = { authViewModel.signOut() }

    Scaffold(
        bottomBar = {
            when (currentRole) {
                UserRole.VOLUNTEER -> VolunteerNavBar(navController = navController)
                UserRole.NGO -> NgoNavBar(navController = navController)
                null -> {}
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable(Screen.Login.route) {
                LoginScreen(navController = navController, authViewModel = authViewModel)
            }
            composable(Screen.Register.route) {
                RegisterScreen(navController = navController, authViewModel = authViewModel)
            }
            composable(Screen.VolunteerHome.route) {
                HomeScreen(navController = navController)
            }
            composable(
                route = "${Screen.DriveDetail.route}/{driveId}",
                arguments = listOf(navArgument("driveId") { type = NavType.StringType })
            ) { backStackEntry ->
                val driveId = backStackEntry.arguments?.getString("driveId") ?: ""
                DriveDetailScreen(navController = navController, driveId = driveId)
            }
            composable(Screen.Search.route) {
                SearchScreen(navController = navController)
            }
            composable(Screen.MyApplications.route) {
                MyApplicationsScreen(navController = navController)
            }
            composable(Screen.VolunteerProfile.route) {
                ProfileScreen(navController = navController, onSignOut = onSignOut)
            }
            composable(Screen.NgoDashboard.route) {
                NgoDashboardScreen(navController = navController)
            }
            composable(Screen.CreateDrive.route) {
                CreateDriveScreen(navController = navController)
            }
            composable(Screen.ManageDrives.route) {
                ManageDrivesScreen(navController = navController)
            }
            composable(Screen.NgoProfile.route) {
                NgoProfileScreen(navController = navController, onSignOut = onSignOut)
            }
            composable(
                route = "${Screen.NgoApplications.route}/{driveId}",
                arguments = listOf(navArgument("driveId") { type = NavType.StringType })
            ) { backStackEntry ->
                val driveId = backStackEntry.arguments?.getString("driveId") ?: ""
                DriveApplicationsScreen(navController = navController, driveId = driveId)
            }
        }
    }
}
