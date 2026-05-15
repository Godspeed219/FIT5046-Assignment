package com.example.assignment_fit5046.services.remote.firebase

import com.example.assignment_fit5046.datamodels.User
import com.example.assignment_fit5046.datamodels.UserRole
import com.example.assignment_fit5046.services.remote.firebase.FirebaseService.USERS_COLLECTION
import com.example.assignment_fit5046.services.remote.firebase.FirebaseService.auth
import com.example.assignment_fit5046.services.remote.firebase.FirebaseService.currentUserId
import com.example.assignment_fit5046.services.remote.firebase.FirebaseService.firestore
import com.google.firebase.auth.GoogleAuthProvider
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

    suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: throw Exception("No Firebase user")
            val uid = firebaseUser.uid
            val snapshot = firestore.collection(USERS_COLLECTION).document(uid).get().await()
            if (snapshot.exists()) {
                val user = snapshot.toObject(User::class.java)!!
                Result.success(user)
            } else {
                val user = User(
                    uid = uid,
                    email = firebaseUser.email ?: "",
                    name = firebaseUser.displayName ?: "",
                    role = UserRole.VOLUNTEER,
                    phoneNumber = "",
                    bio = "",
                    profileImageUrl = "",
                    ngoName = "",
                    ngoDescription = ""
                )
                firestore.collection(USERS_COLLECTION).document(uid).set(user).await()
                Result.success(user)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
