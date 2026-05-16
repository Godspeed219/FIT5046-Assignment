package com.example.assignment_fit5046.services.remote.firebase

import com.example.assignment_fit5046.datamodels.AppNotification
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

object NotificationService {

    private val db = FirebaseFirestore.getInstance()

    suspend fun sendNotification(notification: AppNotification) {
        try {
            val docRef = db.collection("notifications").document()
            val withId = notification.copy(notificationId = docRef.id)
            docRef.set(withId).await()
        } catch (_: Exception) {
            // Fail silently — notification send is non-critical
        }
    }

    fun listenForNotifications(
        uid: String,
        onNotificationsChanged: (List<AppNotification>) -> Unit
    ): ListenerRegistration {
        return db.collection("notifications")
            .whereEqualTo("recipientUid", uid)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener
                val notifications = snapshot.documents
                    .mapNotNull { doc -> doc.toObject(AppNotification::class.java) }
                    .sortedByDescending { it.createdAt }
                onNotificationsChanged(notifications)
            }
    }

    suspend fun markAsRead(notificationId: String) {
        try {
            db.collection("notifications").document(notificationId)
                .update("read", true).await()
        } catch (_: Exception) {}
    }
}
