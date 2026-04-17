package com.example.assignment_fit5046.screens.volunteer

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.assignment_fit5046.components.common.Screen
import com.example.assignment_fit5046.components.volunteer.DriveCard
import com.example.assignment_fit5046.components.volunteer.QuoteCard
import com.example.assignment_fit5046.datamodels.DummyData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("VolunteerLink") })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                QuoteCard(quote = DummyData.QUOTE)
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                Text(
                    text = "Upcoming Drives Near You",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            items(DummyData.DRIVES) { drive ->
                DriveCard(drive = drive) {
                    navController.navigate("${Screen.DriveDetail.route}/${drive.driveId}")
                }
            }
        }
    }
}
