package com.example.assignment_fit5046.services.remote.firebase

import com.example.assignment_fit5046.datamodels.Application
import com.example.assignment_fit5046.datamodels.ApplicationStatus
import com.example.assignment_fit5046.services.remote.firebase.FirebaseService.APPLICATIONS_COLLECTION
import com.example.assignment_fit5046.services.remote.firebase.FirebaseService.firestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

object ApplicationService {

    suspend fun applyToDrive(
        driveId: String,
        driveTitle: String,
        volunteerId: String,
        volunteerName: String,
        message: String = ""
    ): Result<Application> = runCatching {
        val existing = firestore.collection(APPLICATIONS_COLLECTION)
            .whereEqualTo("driveId", driveId)
            .whereEqualTo("volunteerId", volunteerId)
            .get()
            .await()
        if (!existing.isEmpty) throw Exception("Already applied to this drive")

        val docRef = firestore.collection(APPLICATIONS_COLLECTION).document()
        val application = Application(
            applicationId = docRef.id,
            driveId = driveId,
            driveTitle = driveTitle,
            volunteerId = volunteerId,
            volunteerName = volunteerName,
            message = message,
            status = ApplicationStatus.PENDING,
            appliedAt = System.currentTimeMillis()
        )
        docRef.set(application).await()
        DriveService.incrementVolunteerCount(driveId)
        application
    }

    suspend fun getApplicationsByVolunteer(volunteerId: String): Result<List<Application>> = runCatching {
        val snapshot = firestore.collection(APPLICATIONS_COLLECTION)
            .whereEqualTo("volunteerId", volunteerId)
            .orderBy("appliedAt", Query.Direction.DESCENDING)
            .get()
            .await()
        snapshot.documents.mapNotNull { it.toObject(Application::class.java) }
    }

    suspend fun getApplicationsByDrive(driveId: String): Result<List<Application>> = runCatching {
        val snapshot = firestore.collection(APPLICATIONS_COLLECTION)
            .whereEqualTo("driveId", driveId)
            .orderBy("appliedAt", Query.Direction.DESCENDING)
            .get()
            .await()
        snapshot.documents.mapNotNull { it.toObject(Application::class.java) }
    }

    suspend fun updateApplicationStatus(
        applicationId: String,
        status: ApplicationStatus
    ): Result<Unit> = runCatching {
        firestore.collection(APPLICATIONS_COLLECTION)
            .document(applicationId)
            .update("status", status.name)
            .await()

        if (status == ApplicationStatus.REJECTED) {
            val snapshot = firestore.collection(APPLICATIONS_COLLECTION)
                .document(applicationId)
                .get()
                .await()
            val application = snapshot.toObject(Application::class.java)!!
            DriveService.decrementVolunteerCount(application.driveId)
        }
    }

    suspend fun withdrawApplication(applicationId: String): Result<Unit> = runCatching {
        val snapshot = firestore.collection(APPLICATIONS_COLLECTION)
            .document(applicationId)
            .get()
            .await()
        val application = snapshot.toObject(Application::class.java)!!
        firestore.collection(APPLICATIONS_COLLECTION).document(applicationId).delete().await()
        DriveService.decrementVolunteerCount(application.driveId)
    }

    suspend fun hasApplied(driveId: String, volunteerId: String): Result<Boolean> = runCatching {
        val snapshot = firestore.collection(APPLICATIONS_COLLECTION)
            .whereEqualTo("driveId", driveId)
            .whereEqualTo("volunteerId", volunteerId)
            .get()
            .await()
        !snapshot.isEmpty
    }
}
