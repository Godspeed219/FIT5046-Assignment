package com.example.assignment_fit5046.services

import android.net.Uri
import com.example.assignment_fit5046.services.FirebaseService.storage
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

object StorageService {

    suspend fun uploadProfileImage(uid: String, imageUri: Uri): Result<String> = runCatching {
        val ref = storage.reference.child("profile_images/$uid/profile.jpg")
        ref.putFile(imageUri).await()
        val url = ref.downloadUrl.await().toString()
        UserService.updateProfileImage(uid, url)
        url
    }

    suspend fun uploadDriveBanner(driveId: String, imageUri: Uri): Result<String> = runCatching {
        val ref = storage.reference.child("drive_banners/$driveId/banner.jpg")
        ref.putFile(imageUri).await()
        ref.downloadUrl.await().toString()
    }

    suspend fun deleteImage(imageUrl: String): Result<Unit> = runCatching {
        FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl).delete().await()
    }
}
