package com.example.assignment_fit5046.components.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.assignment_fit5046.components.ngo.NgoNavBar
import com.example.assignment_fit5046.components.volunteer.VolunteerNavBar
import com.example.assignment_fit5046.datamodels.UserRole
import com.example.assignment_fit5046.screens.common.LoginScreen
import com.example.assignment_fit5046.screens.common.NotificationsScreen
import com.example.assignment_fit5046.screens.common.RegisterScreen
import com.example.assignment_fit5046.screens.company.AboutUsScreen
import com.example.assignment_fit5046.screens.company.ContactUsScreen
import com.example.assignment_fit5046.screens.company.TermsConditionsScreen
import com.example.assignment_fit5046.screens.ngo.CreateDriveScreen
import com.example.assignment_fit5046.screens.ngo.DriveApplicationsScreen
import com.example.assignment_fit5046.screens.ngo.DriveConfirmationScreen
import com.example.assignment_fit5046.screens.ngo.EditDriveScreen
import com.example.assignment_fit5046.screens.ngo.EditNgoProfileScreen
import com.example.assignment_fit5046.screens.ngo.ManageDrivesScreen
import com.example.assignment_fit5046.screens.ngo.NgoDashboardScreen
import com.example.assignment_fit5046.screens.ngo.NgoProfileScreen
import com.example.assignment_fit5046.screens.volunteer.DriveDetailScreen
import com.example.assignment_fit5046.screens.volunteer.EditVolunteerProfileScreen
import com.example.assignment_fit5046.screens.volunteer.HomeScreen
import com.example.assignment_fit5046.screens.volunteer.MyApplicationsScreen
import com.example.assignment_fit5046.screens.volunteer.ProfileScreen
import com.example.assignment_fit5046.services.viewmodel.AuthState
import com.example.assignment_fit5046.services.viewmodel.AuthViewModel
import com.example.assignment_fit5046.services.viewmodel.MainViewModel
import com.example.assignment_fit5046.services.viewmodel.PendingGoogleUser

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
    object DriveConfirmation : Screen("drive_confirmation")
    object EditDrive : Screen("edit_drive")
    object EditNgoProfile : Screen("edit_ngo_profile")
    object EditVolunteerProfile : Screen("edit_volunteer_profile")
    object AboutUs : Screen("about_us")
    object ContactUs : Screen("contact_us")
    object TermsConditions : Screen("terms_conditions")
    object Notifications : Screen("notifications")
}

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    onRoleChanged: (UserRole) -> Unit = {}
) {
    val authState by authViewModel.authState.collectAsState()
    val pendingGoogleUser by authViewModel.pendingGoogleUser.collectAsState()

    if (authState is AuthState.Loading) {
        Box(modifier = Modifier.fillMaxSize()) {
            AppLoader(isLoading = true, role = UserRole.VOLUNTEER)
        }
        return
    }

    val navController = rememberNavController()
    val mainViewModel: MainViewModel = viewModel()

    val startDestination = when (val s = authState) {
        is AuthState.LoggedIn -> if (s.user.role == UserRole.VOLUNTEER)
            Screen.VolunteerHome.route else Screen.NgoDashboard.route
        else -> Screen.Login.route
    }

    var currentRole by remember { mutableStateOf((authState as? AuthState.LoggedIn)?.user?.role) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val volunteerTopRoutes = setOf(
        Screen.VolunteerHome.route,
        Screen.MyApplications.route,
        Screen.VolunteerProfile.route
    )
    val ngoTopRoutes = setOf(
        Screen.NgoDashboard.route,
        Screen.CreateDrive.route,
        Screen.ManageDrives.route,
        Screen.NgoProfile.route
    )

    LaunchedEffect(authState, pendingGoogleUser) {
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
                if (pendingGoogleUser != null) {
                    navController.navigate(Screen.Register.route) {
                        popUpTo(0) { inclusive = true }
                    }
                } else {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            else -> {}
        }
    }

    Scaffold(
        bottomBar = {
            when (currentRole) {
                UserRole.VOLUNTEER -> if (currentRoute in volunteerTopRoutes) VolunteerNavBar(navController = navController)
                UserRole.NGO -> if (currentRoute in ngoTopRoutes) NgoNavBar(navController = navController)
                null -> {}
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
        ) {
            composable(Screen.Login.route) {
                LoginScreen(navController = navController, authViewModel = authViewModel)
            }
            composable(Screen.Register.route) {
                RegisterScreen(navController = navController, authViewModel = authViewModel)
            }
            composable(Screen.VolunteerHome.route) {
                HomeScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    mainViewModel = mainViewModel
                )
            }
            composable(
                route = "${Screen.DriveDetail.route}/{driveId}",
                arguments = listOf(navArgument("driveId") { type = NavType.StringType })
            ) { backStackEntry ->
                val driveId = backStackEntry.arguments?.getString("driveId") ?: ""
                DriveDetailScreen(
                    navController = navController,
                    driveId = driveId,
                    authViewModel = authViewModel,
                    mainViewModel = mainViewModel
                )
            }
            composable(Screen.MyApplications.route) {
                MyApplicationsScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    mainViewModel = mainViewModel
                )
            }
            composable(Screen.VolunteerProfile.route) {
                ProfileScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    mainViewModel = mainViewModel
                )
            }
            composable(Screen.EditVolunteerProfile.route) {
                EditVolunteerProfileScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    mainViewModel = mainViewModel
                )
            }
            composable(Screen.NgoDashboard.route) {
                NgoDashboardScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    mainViewModel = mainViewModel
                )
            }
            composable(Screen.CreateDrive.route) {
                CreateDriveScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    mainViewModel = mainViewModel
                )
            }
            composable(Screen.DriveConfirmation.route) {
                DriveConfirmationScreen(navController = navController)
            }
            composable(Screen.ManageDrives.route) {
                ManageDrivesScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    mainViewModel = mainViewModel
                )
            }
            composable(Screen.NgoProfile.route) {
                NgoProfileScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    mainViewModel = mainViewModel
                )
            }
            composable(
                route = "${Screen.NgoApplications.route}/{driveId}",
                arguments = listOf(navArgument("driveId") { type = NavType.StringType })
            ) { backStackEntry ->
                val driveId = backStackEntry.arguments?.getString("driveId") ?: ""
                DriveApplicationsScreen(
                    navController = navController,
                    driveId = driveId,
                    mainViewModel = mainViewModel
                )
            }
            composable(
                route = "${Screen.EditDrive.route}/{driveId}",
                arguments = listOf(navArgument("driveId") { type = NavType.StringType })
            ) { backStackEntry ->
                EditDriveScreen(
                    driveId = backStackEntry.arguments?.getString("driveId") ?: "",
                    navController = navController,
                    mainViewModel = mainViewModel
                )
            }
            composable(Screen.EditNgoProfile.route) {
                EditNgoProfileScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    mainViewModel = mainViewModel
                )
            }
            composable(Screen.AboutUs.route) {
                AboutUsScreen(navController = navController)
            }
            composable(Screen.ContactUs.route) {
                ContactUsScreen(navController = navController)
            }
            composable(Screen.TermsConditions.route) {
                TermsConditionsScreen(navController = navController)
            }
            composable(Screen.Notifications.route) {
                NotificationsScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    mainViewModel = mainViewModel
                )
            }
        }
    }
}
