package com.example.assignment_fit5046.screens.volunteer

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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.assignment_fit5046.ui.StatusApproved
import com.example.assignment_fit5046.ui.StatusPending
import com.example.assignment_fit5046.ui.StatusRejected
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.assignment_fit5046.datamodels.Application
import com.example.assignment_fit5046.datamodels.ApplicationStatus
import com.example.assignment_fit5046.datamodels.DummyData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApplicationsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Applications") })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            items(DummyData.APPLICATIONS) { application ->
                ApplicationItem(application = application)
            }
        }
    }
}

@Composable
private fun ApplicationItem(application: Application) {
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val formattedDate = dateFormatter.format(Date(application.appliedAt))

    val matchedDrive = DummyData.DRIVES.find { it.driveId == application.driveId }
    val ngoName = matchedDrive?.ngoName ?: ""

    val (statusLabel, statusColor) = when (application.status) {
        ApplicationStatus.APPROVED -> "Accepted" to StatusApproved
        ApplicationStatus.PENDING -> "Pending" to StatusPending
        ApplicationStatus.REJECTED -> "Rejected" to StatusRejected
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = application.driveTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (ngoName.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = ngoName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                    text = "Applied: $formattedDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            AssistChip(
                onClick = {},
                label = { Text(statusLabel, style = MaterialTheme.typography.labelMedium) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = statusColor.copy(alpha = 0.15f),
                    labelColor = statusColor
                ),
                border = AssistChipDefaults.assistChipBorder(
                    borderColor = statusColor.copy(alpha = 0.4f),
                    enabled = true
                )
            )
        }
    }
}
