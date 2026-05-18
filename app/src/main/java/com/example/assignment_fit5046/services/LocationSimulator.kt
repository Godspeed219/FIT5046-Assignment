package com.example.assignment_fit5046.services

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class SimulatedLocation(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val date: String,
    val time: String
)

object LocationSimulator {

    private val _currentLocation = MutableStateFlow<SimulatedLocation?>(null)
    val currentLocation: StateFlow<SimulatedLocation?> = _currentLocation

    private var points: List<SimulatedLocation> = emptyList()
    private var currentIndex = 0

    fun loadFromAssets(context: Context) {
        if (points.isNotEmpty()) return
        try {
            val lines = context.assets.open("geolife_melbourne.csv")
                .bufferedReader().readLines()
            // Skip header line
            points = lines.drop(1).mapNotNull { line ->
                val parts = line.split(",")
                if (parts.size < 5) return@mapNotNull null
                try {
                    SimulatedLocation(
                        latitude = parts[0].trim().toDouble(),
                        longitude = parts[1].trim().toDouble(),
                        altitude = parts[2].trim().toDouble(),
                        date = parts[3].trim(),
                        time = parts[4].trim()
                    )
                } catch (e: Exception) { null }
            }
            Log.d("LocationSimulator", "Loaded ${points.size} GPS points from CSV")
        } catch (e: Exception) {
            Log.e("LocationSimulator", "Failed to load CSV: ${e.message}")
        }
    }

    fun advance() {
        if (points.isEmpty()) return
        currentIndex = (currentIndex + 1) % points.size
        _currentLocation.value = points[currentIndex]
        Log.d("LocationSimulator", "Advanced to point $currentIndex: ${points[currentIndex]}")
    }

    fun getCurrentLocation(): SimulatedLocation? = _currentLocation.value

    fun initIfNeeded(context: Context) {
        loadFromAssets(context)
        if (_currentLocation.value == null && points.isNotEmpty()) {
            _currentLocation.value = points[0]
        }
    }
}
