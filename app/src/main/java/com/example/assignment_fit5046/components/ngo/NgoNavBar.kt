package com.example.assignment_fit5046.components.ngo

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.assignment_fit5046.components.common.Screen

@Composable
fun NgoNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        Triple("Dashboard", Screen.NgoDashboard.route, Icons.Default.Home),
        Triple("Create", Screen.CreateDrive.route, Icons.Default.Add),
        Triple("Manage", Screen.ManageDrives.route, Icons.Default.Edit),
        Triple("Profile", Screen.NgoProfile.route, Icons.Default.Person)
    )

    NavigationBar {
        items.forEach { (label, route, icon) ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick = {
                    navController.navigate(route) {
                        popUpTo(Screen.NgoDashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(imageVector = icon, contentDescription = label) },
                label = { Text(label) }
            )
        }
    }
}
