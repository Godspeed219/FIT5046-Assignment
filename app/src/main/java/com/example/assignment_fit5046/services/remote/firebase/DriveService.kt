package com.example.assignment_fit5046.services.remote.firebase

import com.example.assignment_fit5046.datamodels.Drive
import com.example.assignment_fit5046.datamodels.DriveStatus
import com.example.assignment_fit5046.services.remote.firebase.FirebaseService.APPLICATIONS_COLLECTION
import com.example.assignment_fit5046.services.remote.firebase.FirebaseService.DRIVES_COLLECTION
import com.example.assignment_fit5046.services.remote.firebase.FirebaseService.firestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

object DriveService {

    suspend fun createDrive(drive: Drive): Result<Drive> = runCatching {
        val docRef = firestore.collection(DRIVES_COLLECTION).document()
        val newDrive = drive.copy(
            driveId = docRef.id,
            createdAt = System.currentTimeMillis()
        )
        docRef.set(newDrive).await()
        newDrive
    }

    suspend fun getDriveById(driveId: String): Result<Drive> = runCatching {
        val snapshot = firestore.collection(DRIVES_COLLECTION).document(driveId).get().await()
        if (!snapshot.exists()) throw Exception("Drive not found")
        snapshot.toObject(Drive::class.java)!!
    }

    suspend fun getAllActiveDrives(): Result<List<Drive>> = runCatching {
        val snapshot = firestore.collection(DRIVES_COLLECTION)
            .whereEqualTo("status", DriveStatus.ACTIVE.name)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
        snapshot.documents.mapNotNull { it.toObject(Drive::class.java) }
    }

    suspend fun getDrivesByNgo(ngoId: String): Result<List<Drive>> = runCatching {
        val snapshot = firestore.collection(DRIVES_COLLECTION)
            .whereEqualTo("ngoId", ngoId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
        snapshot.documents.mapNotNull { it.toObject(Drive::class.java) }
    }

    suspend fun updateDrive(drive: Drive): Result<Unit> = runCatching {
        firestore.collection(DRIVES_COLLECTION)
            .document(drive.driveId)
            .set(drive, SetOptions.merge())
            .await()
    }

    suspend fun updateDriveStatus(driveId: String, status: DriveStatus): Result<Unit> = runCatching {
        firestore.collection(DRIVES_COLLECTION)
            .document(driveId)
            .update("status", status.name)
            .await()
    }

    suspend fun deleteDrive(driveId: String): Result<Unit> = runCatching {
        firestore.collection(DRIVES_COLLECTION).document(driveId).delete().await()

        val appsSnapshot = firestore.collection(APPLICATIONS_COLLECTION)
            .whereEqualTo("driveId", driveId)
            .get()
            .await()
        if (appsSnapshot.documents.isNotEmpty()) {
            val batch = firestore.batch()
            appsSnapshot.documents.forEach { batch.delete(it.reference) }
            batch.commit().await()
        }
    }

    suspend fun incrementVolunteerCount(driveId: String): Result<Unit> = runCatching {
        firestore.collection(DRIVES_COLLECTION)
            .document(driveId)
            .update("currentVolunteers", FieldValue.increment(1))
            .await()
    }

    suspend fun decrementVolunteerCount(driveId: String): Result<Unit> = runCatching {
        firestore.collection(DRIVES_COLLECTION)
            .document(driveId)
            .update("currentVolunteers", FieldValue.increment(-1))
            .await()
    }
}
