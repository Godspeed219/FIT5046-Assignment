package com.example.assignment_fit5046.screens.common

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.assignment_fit5046.R
import com.example.assignment_fit5046.components.common.LottieEmptyState
import com.example.assignment_fit5046.datamodels.AppNotification
import com.example.assignment_fit5046.datamodels.UserRole
import com.example.assignment_fit5046.services.viewmodel.AuthState
import com.example.assignment_fit5046.services.viewmodel.AuthViewModel
import com.example.assignment_fit5046.services.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel
) {
    val authState by authViewModel.authState.collectAsState()
    val currentUser = (authState as? AuthState.LoggedIn)?.user
    val notifications by mainViewModel.notifications.collectAsState()

    val role = currentUser?.role ?: UserRole.VOLUNTEER

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (notifications.any { !it.read }) {
                        TextButton(onClick = {
                            notifications.filter { !it.read }.forEach { n ->
                                mainViewModel.markNotificationRead(n.notificationId)
                            }
                        }) {
                            Text("Mark all read")
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
            if (notifications.isEmpty()) {
                LottieEmptyState(
                    rawRes = R.raw.empty_inbox,
                    title = "No notifications",
                    subtitle = "You're all caught up!"
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(notifications, key = { it.notificationId }) { notification ->
                        NotificationItem(
                            notification = notification,
                            onClick = {
                                if (!notification.read) {
                                    mainViewModel.markNotificationRead(notification.notificationId)
                                }
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(notification: AppNotification, onClick: () -> Unit) {
    val unreadBg = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (!notification.read) unreadBg else MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            modifier = Modifier.size(24.dp).padding(top = 2.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (!notification.read) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )
                if (!notification.read) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatTimestamp(notification.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatTimestamp(ts: Long): String =
    SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(ts))
