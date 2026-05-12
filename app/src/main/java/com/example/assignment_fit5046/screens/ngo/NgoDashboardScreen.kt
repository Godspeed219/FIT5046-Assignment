package com.example.assignment_fit5046.screens.ngo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.assignment_fit5046.components.common.Screen
import com.example.assignment_fit5046.components.ngo.DriveManageCard
import com.example.assignment_fit5046.datamodels.ApplicationStatus
import com.example.assignment_fit5046.services.viewmodel.AuthState
import com.example.assignment_fit5046.services.viewmodel.AuthViewModel
import com.example.assignment_fit5046.services.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NgoDashboardScreen(
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

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { mainViewModel.loadNgoDashboard(it) }
    }

    LaunchedEffect(errorMessage, successMessage) {
        val msg = errorMessage ?: successMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            mainViewModel.clearMessages()
        }
    }

    val totalDrives = ngoDrives.size
    val totalApplicants = ngoApplications.size
    val pendingCount = ngoApplications.count { it.status == ApplicationStatus.PENDING }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("VolunteerLink") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "Welcome back,",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currentUser?.ngoName ?: currentUser?.name ?: "",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = { Icon(Icons.Default.Campaign, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary) },
                        value = "$totalDrives",
                        label = "Drives"
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = { Icon(Icons.Default.People, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary) },
                        value = "$totalApplicants",
                        label = "Applicants"
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = { Icon(Icons.AutoMirrored.Filled.Assignment, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary) },
                        value = "$pendingCount",
                        label = "Pending"
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    }
                }
            }

            item {
                Text(
                    text = "Your Active Drives",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            items(ngoDrives, key = { it.driveId }) { drive ->
                val appCount = ngoApplications.count { it.driveId == drive.driveId }
                DriveManageCard(
                    drive = drive,
                    applicationCount = appCount,
                    onViewApplications = {
                        navController.navigate("${Screen.NgoApplications.route}/${drive.driveId}")
                    }
                )
            }

            if (!isLoading && ngoDrives.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No drives yet. Create your first drive!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    value: String,
    label: String
) {
    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            icon()
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
