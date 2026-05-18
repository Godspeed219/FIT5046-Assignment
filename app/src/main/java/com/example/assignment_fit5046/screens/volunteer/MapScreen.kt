package com.example.assignment_fit5046.screens.volunteer

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.assignment_fit5046.components.common.Screen
import com.example.assignment_fit5046.services.LocationSimulator
import com.example.assignment_fit5046.services.viewmodel.MainViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavController,
    mainViewModel: MainViewModel
) {
    val context = LocalContext.current
    val allActiveDrives by mainViewModel.allActiveDrives.collectAsState()
    val driveCoordinates by mainViewModel.driveCoordinatesState.collectAsState()
    val simulatedLocation by LocationSimulator.currentLocation.collectAsState()
    val unreadCount by mainViewModel.unreadCount.collectAsState()

    // Initialise OSMDroid config
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", android.content.Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = context.packageName
    }

    // OSMDroid requires lifecycle handling
    DisposableEffect(Unit) {
        onDispose {
            // MapView will be garbage collected — no explicit cleanup needed in Compose
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Drive Map") },
                actions = {
                    BadgedBox(badge = {
                        if (unreadCount > 0) Badge { Text("$unreadCount") }
                    }) {
                        IconButton(onClick = {
                            navController.navigate(Screen.Notifications.route)
                        }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(13.0)
                        // Centre on Melbourne CBD initially
                        controller.setCenter(GeoPoint(-37.8136, 144.9631))
                    }
                },
                update = { mapView ->
                    mapView.overlays.clear()

                    // Draw simulated location as blue marker
                    simulatedLocation?.let { loc ->
                        val userPoint = GeoPoint(loc.latitude, loc.longitude)

                        // Blue dot marker for user location
                        val userMarker = Marker(mapView).apply {
                            position = userPoint
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            title = "Your Simulated Location"
                            snippet = "${String.format("%.4f", loc.latitude)}, ${String.format("%.4f", loc.longitude)}"
                            icon = ContextCompat.getDrawable(
                                mapView.context,
                                android.R.drawable.ic_menu_mylocation
                            )
                        }
                        mapView.overlays.add(userMarker)

                        // Draw 5km radius circle around user
                        val circle = Polygon(mapView).apply {
                            points = Polygon.pointsAsCircle(userPoint, 5000.0)
                            fillPaint.color = android.graphics.Color.argb(40, 33, 150, 243)
                            outlinePaint.color = android.graphics.Color.argb(180, 33, 150, 243)
                            outlinePaint.strokeWidth = 3f
                        }
                        mapView.overlays.add(circle)

                        // Centre map on user location
                        mapView.controller.animateTo(userPoint)
                    }

                    // Draw drive pins
                    allActiveDrives.forEach { drive ->
                        val coords = driveCoordinates[drive.driveId]
                        if (coords != null) {
                            val drivePoint = GeoPoint(coords.first, coords.second)
                            val driveMarker = Marker(mapView).apply {
                                position = drivePoint
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                title = drive.title
                                snippet = "${drive.category} · ${drive.date} · ${drive.maxVolunteers - drive.currentVolunteers} spots left"
                                // Tap marker to navigate to drive detail
                                setOnMarkerClickListener { marker, _ ->
                                    marker.showInfoWindow()
                                    navController.navigate("${Screen.DriveDetail.route}/${drive.driveId}")
                                    true
                                }
                            }
                            mapView.overlays.add(driveMarker)
                        }
                    }

                    mapView.invalidate()
                },
                modifier = Modifier.fillMaxSize()
            )

            // Legend card bottom left
            Card(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Context-Aware Map",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Blue dot: Simulated GPS location",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Circle: 5km proximity radius",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Pins: Active drives near you",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    simulatedLocation?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "GPS: ${String.format("%.4f", it.latitude)}, ${String.format("%.4f", it.longitude)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
