package com.example.assignment_fit5046.services

import android.util.Log
import com.example.assignment_fit5046.datamodels.User
import com.example.assignment_fit5046.datamodels.UserRole
import com.example.assignment_fit5046.services.FirebaseService.USERS_COLLECTION
import com.example.assignment_fit5046.services.FirebaseService.auth
import com.example.assignment_fit5046.services.FirebaseService.currentUserId
import com.example.assignment_fit5046.services.FirebaseService.firestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

object UserService {

    suspend fun registerUser(
        email: String,
        password: String,
        name: String,
        role: UserRole,
        phoneNumber: String = "",
        bio: String = "",
        ngoName: String = "",
        ngoDescription: String = ""
    ): Result<User> = runCatching {
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = authResult.user!!.uid
        val user = User(
            uid = uid,
            email = email,
            name = name,
            role = role,
            phoneNumber = phoneNumber,
            bio = bio,
            ngoName = ngoName,
            ngoDescription = ngoDescription,
            profileImageUrl = ""
        )
        firestore.collection(USERS_COLLECTION).document(uid).set(user).await()
        user
    }

    suspend fun loginUser(email: String, password: String): Result<User> = runCatching {
        val authResult = auth.signInWithEmailAndPassword(email, password).await()
        val uid = authResult.user!!.uid
        val snapshot = firestore.collection(USERS_COLLECTION).document(uid).get().await()
        snapshot.toObject(User::class.java)!!
    }

    suspend fun getCurrentUser(): Result<User> = runCatching {
        val uid = currentUserId ?: throw Exception("Not logged in")
        val snapshot = firestore.collection(USERS_COLLECTION).document(uid).get().await()
        snapshot.toObject(User::class.java)!!
    }

    suspend fun updateUser(user: User): Result<Unit> = runCatching {
        firestore.collection(USERS_COLLECTION)
            .document(user.uid)
            .set(user, SetOptions.merge())
            .await()
    }

    suspend fun updateProfileImage(uid: String, imageUrl: String): Result<Unit> = runCatching {
        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .update("profileImageUrl", imageUrl)
            .await()
    }

    fun logoutUser() {
        auth.signOut()
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email).await()
    }
}
