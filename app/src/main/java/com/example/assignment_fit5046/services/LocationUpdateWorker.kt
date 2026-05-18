package com.example.assignment_fit5046.services

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class LocationUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            Log.d("LocationUpdateWorker", "Worker running — advancing location")
            LocationSimulator.initIfNeeded(applicationContext)
            LocationSimulator.advance()
            val loc = LocationSimulator.getCurrentLocation()
            Log.d("LocationUpdateWorker", "New location: lat=${loc?.latitude}, lon=${loc?.longitude}")
            Result.success()
        } catch (e: Exception) {
            Log.e("LocationUpdateWorker", "Worker failed: ${e.message}")
            Result.retry()
        }
    }
}
