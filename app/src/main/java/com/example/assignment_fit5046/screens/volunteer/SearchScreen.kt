// Deprecated: search and filter functionality has been merged into HomeScreen.
package com.example.assignment_fit5046.screens.volunteer

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.assignment_fit5046.components.common.AppLoader
import com.example.assignment_fit5046.components.common.Screen
import com.example.assignment_fit5046.components.volunteer.DriveCard
import com.example.assignment_fit5046.datamodels.GlobalGivingProject
import com.example.assignment_fit5046.datamodels.UserRole
import com.example.assignment_fit5046.services.viewmodel.MainViewModel

private val CAUSE_OPTIONS = listOf(
    "All", "Environment", "Education", "Health", "Animal Welfare", "Community"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    mainViewModel: MainViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var expanded by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    val searchResults by mainViewModel.searchResults.collectAsState()
    val ngoSearchResults by mainViewModel.ngoSearchResults.collectAsState()
    val isLoading by mainViewModel.isLoading.collectAsState()
    val isRefreshing by mainViewModel.isRefreshing.collectAsState()
    val pullRefreshState = rememberPullToRefreshState()

    LaunchedEffect(searchQuery, selectedCategory) {
        mainViewModel.searchDrives(searchQuery, selectedCategory)
        mainViewModel.searchGlobalGivingByCategory(selectedCategory)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Search Drives") })
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { mainViewModel.refreshDrives(searchQuery, selectedCategory) },
                state = pullRefreshState,
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            placeholder = { Text("Search drives...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            singleLine = true
                        )
                    }

                    item {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            OutlinedTextField(
                                value = selectedCategory,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Filter by Cause") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                CAUSE_OPTIONS.forEach { cause ->
                                    DropdownMenuItem(
                                        text = { Text(cause) },
                                        onClick = {
                                            selectedCategory = cause
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Drives section header
                    item {
                        Text(
                            text = "Drives",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    if (searchResults.isEmpty()) {
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
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No drives found",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(searchResults, key = { it.driveId }) { drive ->
                            DriveCard(drive = drive) {
                                navController.navigate("${Screen.DriveDetail.route}/${drive.driveId}")
                            }
                        }
                    }

                    // NGO Partners section — only shown when a specific category is selected
                    if (selectedCategory != "All") {
                        item {
                            Text(
                                text = "NGO Partners",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp, end = 16.dp)
                            )
                        }

                        if (ngoSearchResults.isEmpty() && !isLoading) {
                            item {
                                Text(
                                    text = "No NGO projects found for this category.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        } else {
                            items(ngoSearchResults, key = { it.id }) { project ->
                                GlobalGivingProjectCard(
                                    project = project,
                                    onLinkClick = { url ->
                                        try { uriHandler.openUri(url) } catch (_: Exception) {}
                                    }
                                )
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(88.dp)) }
                }
            }

            AppLoader(isLoading = isLoading, role = UserRole.VOLUNTEER)
        }
    }
}

@Composable
private fun GlobalGivingProjectCard(
    project: GlobalGivingProject,
    onLinkClick: (String) -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = project.title ?: "Untitled Project",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            project.organization?.name?.let { orgName ->
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = orgName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            project.summary?.let { summary ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            project.projectLink?.let { url ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onLinkClick(url) }
                ) {
                    Text(
                        text = "View Project",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
