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
import com.example.assignment_fit5046.R
import com.example.assignment_fit5046.components.common.AppLoader
import com.example.assignment_fit5046.components.common.AppToast
import com.example.assignment_fit5046.components.common.Screen
import com.example.assignment_fit5046.components.ngo.DriveManageCard
import com.example.assignment_fit5046.datamodels.DriveStatus
import com.example.assignment_fit5046.datamodels.UserRole
import com.example.assignment_fit5046.services.viewmodel.AuthState
import com.example.assignment_fit5046.services.viewmodel.AuthViewModel
import com.example.assignment_fit5046.services.viewmodel.MainViewModel

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
    val errorMessage by mainViewModel.errorMessage.collectAsState()
    val successMessage by mainViewModel.successMessage.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    val pullRefreshState = rememberPullToRefreshState()

    val currentDrives = ngoDrives.filter { it.status == DriveStatus.ACTIVE }
    val expiredDrives = ngoDrives.filter { it.status == DriveStatus.CLOSED }
    val displayedDrives = if (selectedTabIndex == 0) currentDrives else expiredDrives

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

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.CreateDrive.route) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Drive")
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
                    isRefreshing = isLoading && ngoDrives.isNotEmpty(),
                    onRefresh = { currentUser?.uid?.let { mainViewModel.loadNgoDashboard(it) } },
                    state = pullRefreshState,
                    modifier = Modifier.weight(1f)
                ) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        when {
                            ngoDrives.isEmpty() -> item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 64.dp, start = 48.dp, end = 48.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_empty_drives),
                                        contentDescription = null,
                                        modifier = Modifier.size(180.dp)
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text(
                                        "No drives yet",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Create your first drive and start finding volunteers",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(32.dp))
                                    Button(
                                        onClick = { navController.navigate(Screen.CreateDrive.route) },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Create Your First Drive")
                                    }
                                }
                            }

                            displayedDrives.isEmpty() -> item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        if (selectedTabIndex == 0) "No current drives" else "No expired drives",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                        val newStatus = if (drive.status == DriveStatus.ACTIVE) DriveStatus.CLOSED else DriveStatus.ACTIVE
                                        mainViewModel.updateDriveStatus(drive.driveId, newStatus)
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
