package com.example.projectsigma.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserSummaryDto(
    val id: String = "",
    val userId: String? = null,
    val displayName: String = "User",
    val photoUrl: String? = null,
    val bio: String? = null
) {
    val validId: String
        get() = if (id.isNotBlank()) id else (userId ?: "user_anon")
}

@Serializable
data class UserProfileDto(
    val id: String = "",
    val userId: String? = null,
    val email: String = "",
    val displayName: String = "User",
    val photoUrl: String? = null,
    val bio: String? = null,
    val instagramHandle: String? = null,
    val telegramHandle: String? = null,
    val isLocationVisible: Boolean = true,
    val isSocialsPublic: Boolean = true,
    val friendsCount: Int = 0,
    val createdAt: String? = null
) {
    val validId: String
        get() = if (id.isNotBlank()) id else (userId ?: "user_anon")
}

@Serializable
data class UpdateProfileRequest(
    val displayName: String? = null,
    val bio: String? = null,
    val instagramHandle: String? = null,
    val telegramHandle: String? = null
)

@Serializable
data class UpdatePrivacyRequest(
    val isLocationVisible: Boolean? = null,
    val isSocialsPublic: Boolean? = null
)

@Serializable
data class UpdateFcmTokenRequest(
    val fcmToken: String
)
