package com.example.assignment_fit5046.services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

object FirebaseService {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    val storage: FirebaseStorage = FirebaseStorage.getInstance()

    val currentUser: FirebaseUser? get() = auth.currentUser
    val currentUserId: String? get() = auth.currentUser?.uid
    fun isLoggedIn(): Boolean = auth.currentUser != null

    const val USERS_COLLECTION = "users"
    const val DRIVES_COLLECTION = "drives"
    const val APPLICATIONS_COLLECTION = "applications"
}
