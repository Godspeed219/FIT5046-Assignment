package com.example.assignment_fit5046.screens.volunteer

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
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.assignment_fit5046.components.common.AppLoader
import com.example.assignment_fit5046.components.common.AppToast
import com.example.assignment_fit5046.components.common.Screen
import com.example.assignment_fit5046.components.volunteer.DriveCard
import com.example.assignment_fit5046.components.volunteer.QuoteCard
import com.example.assignment_fit5046.datamodels.UserRole
import com.example.assignment_fit5046.services.viewmodel.AuthState
import com.example.assignment_fit5046.services.viewmodel.AuthViewModel
import com.example.assignment_fit5046.services.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel
) {
    val authState by authViewModel.authState.collectAsState()
    val currentUser = (authState as? AuthState.LoggedIn)?.user

    val allActiveDrives by mainViewModel.allActiveDrives.collectAsState()
    val quote by mainViewModel.quote.collectAsState()
    val isLoading by mainViewModel.isLoading.collectAsState()
    val errorMessage by mainViewModel.errorMessage.collectAsState()

    var toastMessage by remember { mutableStateOf<String?>(null) }
    val pullRefreshState = rememberPullToRefreshState()

    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { mainViewModel.loadVolunteerHome(it) }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            toastMessage = errorMessage
            mainViewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("VolunteerLink") })
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            PullToRefreshBox(
                isRefreshing = isLoading && allActiveDrives.isNotEmpty(),
                onRefresh = { currentUser?.uid?.let { mainViewModel.refreshVolunteerHome(it) } },
                state = pullRefreshState,
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                        ) {
                            Text(
                                text = "Welcome back,",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = currentUser?.name ?: "Volunteer",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    item {
                        quote?.let {
                            QuoteCard(quote = it)
                        }
                    }

                    item {
                        Text(
                            text = "Upcoming Drives Near You",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    if (!isLoading && allActiveDrives.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.SearchOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        "No drives available right now",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    items(allActiveDrives, key = { it.driveId }) { drive ->
                        DriveCard(
                            drive = drive,
                            onClick = {
                                navController.navigate("${Screen.DriveDetail.route}/${drive.driveId}")
                            }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }

            AppLoader(
                isLoading = isLoading && allActiveDrives.isEmpty(),
                role = UserRole.VOLUNTEER
            )

            AppToast(
                message = toastMessage ?: "",
                isVisible = toastMessage != null,
                role = UserRole.VOLUNTEER,
                onDismiss = { toastMessage = null }
            )
        }
    }
}
