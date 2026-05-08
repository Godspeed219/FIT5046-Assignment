package com.example.assignment_fit5046.datamodels

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val role: UserRole = UserRole.VOLUNTEER,
    val phoneNumber: String = "",
    val bio: String = "",
    val profileImageUrl: String = "",

    val ngoName: String = "",
    val ngoDescription: String = ""
)

enum class UserRole {
    VOLUNTEER, NGO
}
