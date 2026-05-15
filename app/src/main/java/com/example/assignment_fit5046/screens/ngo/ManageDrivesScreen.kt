package com.example.assignment_fit5046.screens.ngo

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.assignment_fit5046.components.common.AppLoader
import com.example.assignment_fit5046.components.common.AppToast
import com.example.assignment_fit5046.components.common.LottieEmptyState
import com.example.assignment_fit5046.components.common.Screen
import com.example.assignment_fit5046.components.ngo.DriveManageCard
import com.example.assignment_fit5046.datamodels.DriveStatus
import com.example.assignment_fit5046.datamodels.UserRole
import com.example.assignment_fit5046.services.viewmodel.AuthState
import com.example.assignment_fit5046.services.viewmodel.AuthViewModel
import com.example.assignment_fit5046.services.viewmodel.MainViewModel
import android.content.Context
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.Archive
import androidx.compose.ui.platform.LocalContext
import com.example.assignment_fit5046.R
import com.example.assignment_fit5046.datamodels.Drive
import androidx.core.content.edit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageDrivesScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel
) {
    val authState by authViewModel.authState.collectAsState()
    val currentUser = (authState as? AuthState.LoggedIn)?.user

    val ngoDrives by mainViewModel.ngoDrives.collectAsState()
    val ngoApplications by mainViewModel.ngoApplications.collectAsState()
    val isLoading by mainViewModel.isLoading.collectAsState()
    val isRefreshing by mainViewModel.isRefreshing.collectAsState()
    val errorMessage by mainViewModel.errorMessage.collectAsState()
    val successMessage by mainViewModel.successMessage.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var driveToClose by remember { mutableStateOf<Drive?>(null) }
    val pullRefreshState = rememberPullToRefreshState()
    val context = LocalContext.current

    val currentDrives = ngoDrives.filter { it.status == DriveStatus.ACTIVE }
    val expiredDrives = ngoDrives.filter { it.status == DriveStatus.CLOSED }
    val displayedDrives = if (selectedTabIndex == 0) currentDrives else expiredDrives

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val lastCheck = prefs.getLong("last_expire_check", 0L)
        val now = System.currentTimeMillis()
        if (now - lastCheck > 86400000L) {
            currentUser?.uid?.let { mainViewModel.expirePassedDrives(it) }
            prefs.edit { putLong("last_expire_check", now) }
        }
    }

    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { mainViewModel.loadNgoDashboard(it) }
    }

    LaunchedEffect(errorMessage, successMessage) {
        val msg = errorMessage ?: successMessage
        if (msg != null) {
            toastMessage = msg
            mainViewModel.clearMessages()
        }
    }

    driveToClose?.let { drive ->
        AlertDialog(
            onDismissRequest = { driveToClose = null },
            icon = { Icon(Icons.Default.Archive, contentDescription = null) },
            title = { Text("Close this drive?") },
            text = {
                Text("This will permanently close \"${drive.title}\". Volunteers will no longer be able to apply. This cannot be undone.")
            },
            confirmButton = {
                Button(onClick = {
                    mainViewModel.closeDrive(drive.driveId)
                    driveToClose = null
                }) {
                    Text("Yes, Close")
                }
            },
            dismissButton = {
                TextButton(onClick = { driveToClose = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            if (!ngoDrives.isEmpty()) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.CreateDrive.route) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Drive")
                }

            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TabRow(selectedTabIndex = selectedTabIndex) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 }
                    ) {
                        Text("Current", modifier = Modifier.padding(vertical = 12.dp))
                    }
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 }
                    ) {
                        Text("Expired", modifier = Modifier.padding(vertical = 12.dp))
                    }
                }

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { currentUser?.uid?.let { mainViewModel.refreshNgoDrives(it) } },
                    state = pullRefreshState,
                    modifier = Modifier.weight(1f).padding(vertical = 38.dp)
                ) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        when {
                            ngoDrives.isEmpty() -> item {
                                LottieEmptyState(
                                    rawRes = R.raw.no_result,
                                    title = "No active drives",
                                    subtitle = "Create a drive to start finding volunteers",
                                    action = {
                                        Button(onClick = { navController.navigate(Screen.CreateDrive.route) }) {
                                            Text("Create Drive")
                                        }
                                    }
                                )
                            }

                            displayedDrives.isEmpty() -> item {
                                if (selectedTabIndex == 0) {
                                    LottieEmptyState(
                                        rawRes = R.raw.empty_search,
                                        title = "No active drives",
                                        subtitle = "Create a drive to start finding volunteers",
                                        action = {
                                            Button(onClick = { navController.navigate(Screen.CreateDrive.route) }) {
                                                Text("Create Drive")
                                            }
                                        }
                                    )
                                } else {
                                    LottieEmptyState(
                                        rawRes = R.raw.empty_search,
                                        title = "No closed drives",
                                        subtitle = "Drives you close or that expire will appear here"
                                    )
                                }
                            }

                            else -> items(displayedDrives, key = { it.driveId }) { drive ->
                                val appCount = ngoApplications.count { it.driveId == drive.driveId }
                                DriveManageCard(
                                    drive = drive,
                                    applicationCount = appCount,
                                    onViewApplications = {
                                        navController.navigate("${Screen.NgoApplications.route}/${drive.driveId}")
                                    },
                                    onEdit = {
                                        navController.navigate("${Screen.EditDrive.route}/${drive.driveId}")
                                    },
                                    onToggleStatus = {
                                        if (drive.status == DriveStatus.ACTIVE) driveToClose = drive
                                    }
                                )
                            }
                        }

                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }

            AppLoader(
                isLoading = isLoading && ngoDrives.isEmpty(),
                role = UserRole.NGO
            )

            AppToast(
                message = toastMessage ?: "",
                isVisible = toastMessage != null,
                role = UserRole.NGO,
                onDismiss = { toastMessage = null }
            )
        }
    }
}
