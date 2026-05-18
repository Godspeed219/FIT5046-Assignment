package com.example.assignment_fit5046.screens.volunteer

import android.R.attr.singleLine
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.assignment_fit5046.R
import com.example.assignment_fit5046.components.common.AppLoader
import com.example.assignment_fit5046.components.common.AppToast
import com.example.assignment_fit5046.components.common.LottieEmptyState
import com.example.assignment_fit5046.components.common.Screen
import com.example.assignment_fit5046.components.volunteer.DriveCard
import com.example.assignment_fit5046.components.volunteer.QuoteCard
import com.example.assignment_fit5046.datamodels.UserRole
import com.example.assignment_fit5046.services.LocationSimulator
import com.example.assignment_fit5046.services.viewmodel.AuthState
import com.example.assignment_fit5046.services.viewmodel.AuthViewModel
import com.example.assignment_fit5046.services.viewmodel.MainViewModel

private val CAUSE_FILTERS = listOf(
    "All", "Environment", "Education", "Health", "Animal Welfare", "Community"
)

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
    val contextRankedDrives by mainViewModel.contextRankedDrives.collectAsState()
    val quote by mainViewModel.quote.collectAsState()
    val isLoading by mainViewModel.isLoading.collectAsState()
    val isRefreshing by mainViewModel.isRefreshing.collectAsState()
    val errorMessage by mainViewModel.errorMessage.collectAsState()
    val unreadCount by mainViewModel.unreadCount.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    val pullRefreshState = rememberPullToRefreshState()

    val sourceDrives = if (contextRankedDrives.isNotEmpty()) contextRankedDrives else allActiveDrives
    val filteredDrives = sourceDrives.filter { drive ->
        val matchesQuery = searchQuery.isEmpty() ||
            drive.title.contains(searchQuery, ignoreCase = true) ||
            drive.ngoName.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == "All" || drive.category == selectedCategory
        matchesQuery && matchesCategory
    }

    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let {
            mainViewModel.loadVolunteerHome(it)
            mainViewModel.refreshQuote()
        }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            toastMessage = errorMessage
            mainViewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Welcome !") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = Color.Black,
                    actionIconContentColor = MaterialTheme.colorScheme.primary,
                ),
                actions = {
                    BadgedBox(badge = { if (unreadCount > 0) Badge { Text("$unreadCount") } }) {
                        IconButton(onClick = { navController.navigate(Screen.Notifications.route) }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                    }
                }

            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { currentUser?.uid?.let { mainViewModel.refreshVolunteerHome(it) } },
                state = pullRefreshState,
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = currentUser?.name ?: "Volunteer",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    // Quote card
                    item {
                        quote?.let { q ->
                            QuoteCard(quote = q)
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    // Search field
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            placeholder = { Text("Search Drives or NGOs ...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear search")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Category filter chips
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CAUSE_FILTERS.forEach { category ->
                                FilterChip(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category },
                                    label = { Text(category) }
                                )
                            }
                        }
                    }

                    // Section heading
                    item {
                        Text(
                            text = "Upcoming Drives Near You",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    item {
                        val location = LocationSimulator.currentLocation.collectAsState().value
                        if (location != null) {
                            Text(
                                text = "Showing drives near ${String.format("%.4f", location.latitude)}, ${String.format("%.4f", location.longitude)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Empty state
                    if (!isLoading && filteredDrives.isEmpty()) {
                        item {
                            if (searchQuery.isNotEmpty() || selectedCategory != "All") {
                                LottieEmptyState(
                                    rawRes = R.raw.empty_search,
                                    title = "No drives found",
                                    subtitle = "Try a different search or category",
                                    action = {
                                        TextButton(onClick = {
                                            searchQuery = ""
                                            selectedCategory = "All"
                                        }) {
                                            Text("Clear filters")
                                        }
                                    }
                                )
                            } else {
                                LottieEmptyState(
                                    rawRes = R.raw.empty_search,
                                    title = "No drives near you",
                                    subtitle = "Check back soon for new opportunities"
                                )
                            }
                        }
                    }

                    items(filteredDrives, key = { it.driveId }) { drive ->
                        DriveCard(
                            drive = drive,
                            onClick = {
                                navController.navigate("${Screen.DriveDetail.route}/${drive.driveId}")
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(88.dp))
                    }

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
