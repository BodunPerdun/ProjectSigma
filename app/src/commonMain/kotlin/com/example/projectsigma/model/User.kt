package com.example.projectsigma.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val photoUrl: String? = null,
    val eventsCount: Int = 0,
    val isLocationVisible: Boolean = false,
    val isSocialsPublic: Boolean = true,
    val friends: List<String> = emptyList(),
    val avatarSyncStatus: String = "PENDING_PUSH",
    val instagramHandle: String? = null,
    val telegramHandle: String? = null,
    val bio: String? = null
)
