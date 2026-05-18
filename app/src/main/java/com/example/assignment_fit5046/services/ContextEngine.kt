package com.example.assignment_fit5046.services

import android.util.Log
import com.example.assignment_fit5046.datamodels.Application
import com.example.assignment_fit5046.datamodels.Drive
import java.util.Calendar
import kotlin.math.*

object ContextEngine {

    // Haversine distance in km between two lat/lon points
    fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    // Get time-of-day period
    fun getTimePeriod(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "morning"
            in 12..17 -> "afternoon"
            in 18..21 -> "evening"
            else -> "night"
        }
    }

    // Get preferred categories based on application history (non-sensory data)
    fun getPreferredCategories(applications: List<Application>): List<String> {
        return applications
            .groupBy { it.driveTitle }
            .keys
            .take(3)
            .toList()
    }

    // Core context-aware ranking function
    // Combines: proximity (sensory) + time of day (non-sensory) + past applications (non-sensory)
    fun rankDrives(
        drives: List<Drive>,
        location: SimulatedLocation?,
        applications: List<Application>,
        driveCoordinates: Map<String, Pair<Double, Double>>
    ): List<Drive> {
        if (location == null) return drives

        val timePeriod = getTimePeriod()
        val preferredCategories = getPreferredCategories(applications)

        Log.d("ContextEngine", "Ranking ${drives.size} drives")
        Log.d("ContextEngine", "Time period: $timePeriod")
        Log.d("ContextEngine", "User lat: ${location.latitude}, lon: ${location.longitude}")
        Log.d("ContextEngine", "Preferred categories: $preferredCategories")

        return drives.sortedByDescending { drive ->
            var score = 0.0

            // Proximity score (sensory) — closer drives score higher
            val coords = driveCoordinates[drive.driveId]
            if (coords != null) {
                val dist = distanceKm(location.latitude, location.longitude, coords.first, coords.second)
                // Max 40 points — full score under 2km, zero at 20km+
                score += maxOf(0.0, 40.0 - (dist * 2.0))
                Log.d("ContextEngine", "Drive ${drive.title}: distance=${String.format("%.1f", dist)}km, proximity score=${maxOf(0.0, 40.0 - (dist * 2.0))}")
            }

            // Time of day score (non-sensory) — morning drives score higher in morning etc
            val timeScore = when {
                timePeriod == "morning" && drive.startTime.isNotBlank() -> 20.0
                timePeriod == "afternoon" && drive.startTime.isNotBlank() -> 15.0
                timePeriod == "evening" && drive.category == "Community" -> 10.0
                else -> 5.0
            }
            score += timeScore

            // Category preference score (non-sensory) — past application history
            if (preferredCategories.any { it.contains(drive.category, ignoreCase = true) }) {
                score += 20.0
            }

            // Spots urgency score — fewer spots = more urgent = higher priority
            val spotsLeft = drive.maxVolunteers - drive.currentVolunteers
            if (spotsLeft in 1..5) score += 10.0

            Log.d("ContextEngine", "Drive ${drive.title}: total score=$score")
            score
        }
    }
}
