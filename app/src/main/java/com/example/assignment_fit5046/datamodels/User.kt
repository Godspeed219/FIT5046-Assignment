package com.example.volunteerlink_fit5046.datamodels

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val role: UserRole = UserRole.VOLUNTEER,
    val phoneNumber: String = "",
    val bio: String = "",
    val profileImageUrl: String = "",
    // NGO-only fields
    val ngoName: String = "",
    val ngoDescription: String = ""
)

enum class UserRole {
    VOLUNTEER, NGO
}
