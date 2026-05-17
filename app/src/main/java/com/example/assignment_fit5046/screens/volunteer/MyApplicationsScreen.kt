package com.example.assignment_fit5046.screens.volunteer

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.graphics.Color
import com.example.assignment_fit5046.R
import com.example.assignment_fit5046.components.common.AppToast
import com.example.assignment_fit5046.components.common.LottieEmptyState
import com.example.assignment_fit5046.components.common.Screen
import com.example.assignment_fit5046.datamodels.Application
import com.example.assignment_fit5046.datamodels.ApplicationStatus
import com.example.assignment_fit5046.datamodels.UserRole
import com.example.assignment_fit5046.services.viewmodel.AuthState
import com.example.assignment_fit5046.services.viewmodel.AuthViewModel
import com.example.assignment_fit5046.services.viewmodel.MainViewModel
import com.example.assignment_fit5046.ui.StatusApproved
import com.example.assignment_fit5046.ui.StatusApprovedContainer
import com.example.assignment_fit5046.ui.StatusPending
import com.example.assignment_fit5046.ui.StatusPendingContainer
import com.example.assignment_fit5046.ui.StatusRejected
import com.example.assignment_fit5046.ui.StatusRejectedContainer
import com.example.assignment_fit5046.ui.SurfaceVariant
import com.example.assignment_fit5046.ui.TextDisabled
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApplicationsScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel
) {
    val authState by authViewModel.authState.collectAsState()
    val currentUser = (authState as? AuthState.LoggedIn)?.user

    val volunteerApplications by mainViewModel.volunteerApplications.collectAsState()
    val isRefreshing by mainViewModel.isRefreshing.collectAsState()
    val errorMessage by mainViewModel.errorMessage.collectAsState()
    val successMessage by mainViewModel.successMessage.collectAsState()

    var toastMessage by remember { mutableStateOf<String?>(null) }
    var pendingWithdraw by remember { mutableStateOf<Application?>(null) }
    val pullRefreshState = rememberPullToRefreshState()
    val unreadCount by mainViewModel.unreadCount.collectAsState()

    LaunchedEffect(errorMessage, successMessage) {
        val msg = errorMessage ?: successMessage
        if (msg != null) {
            toastMessage = msg
            mainViewModel.clearMessages()
        }
    }

    if (pendingWithdraw != null) {
        AlertDialog(
            onDismissRequest = { pendingWithdraw = null },
            title = { Text("Withdraw Application") },
            text = { Text("Are you sure you want to withdraw your application for \"${pendingWithdraw!!.driveTitle}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        mainViewModel.withdrawApplication(
                            applicationId = pendingWithdraw!!.applicationId,
                            driveId = pendingWithdraw!!.driveId
                        )
                        pendingWithdraw = null
                    }
                ) { Text("Withdraw", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { pendingWithdraw = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Applications") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = Color.Black,
                    actionIconContentColor = MaterialTheme.colorScheme.primary,
                ),
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
                onRefresh = { currentUser?.uid?.let { mainViewModel.refreshMyApplications(it) } },
                state = pullRefreshState,
                modifier = Modifier.fillMaxSize()
            ) {
                if (volunteerApplications.isEmpty()) {
                    LottieEmptyState(
                        rawRes = R.raw.empty_inbox,
                        title = "No applications yet",
                        subtitle = "Start exploring drives and apply to get involved"
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(volunteerApplications, key = { it.applicationId }) { application ->
                            ApplicationCard(
                                application = application,
                                onWithdraw = { pendingWithdraw = application }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(88.dp))
                        }

                    }
                }
            }

            AppToast(
                message = toastMessage ?: "",
                isVisible = toastMessage != null,
                role = UserRole.VOLUNTEER,
                onDismiss = { toastMessage = null }
            )
        }
    }
}


@Composable
private fun ApplicationCard(
    application: Application,
    onWithdraw: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = application.driveTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                StatusChip(status = application.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Applied: ${formatDate(application.appliedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (application.message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = application.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (application.status == ApplicationStatus.PENDING) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onWithdraw,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Withdraw Application")
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: ApplicationStatus) {
    val (label, containerColor, contentColor) = when (status) {
        ApplicationStatus.PENDING -> Triple("Pending", StatusPendingContainer, StatusPending)
        ApplicationStatus.APPROVED -> Triple("Approved", StatusApprovedContainer, StatusApproved)
        ApplicationStatus.REJECTED -> Triple("Rejected", StatusRejectedContainer, StatusRejected)
        ApplicationStatus.WITHDRAWN -> Triple("Withdrawn", SurfaceVariant, TextDisabled)
    }
    AssistChip(
        onClick = {},
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = containerColor,
            labelColor = contentColor
        ),
        border = AssistChipDefaults.assistChipBorder(
            borderColor = contentColor.copy(alpha = 0.4f),
            enabled = true
        )
    )
}

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(timestamp))
