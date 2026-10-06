package com.example.projectsigma.model

import kotlinx.serialization.Serializable

enum class FriendRequestStatus {
    PENDING,
    ACCEPTED,
    DECLINED
}

@Serializable
data class FriendRequest(
    val requestId: String,
    val senderId: String,
    val senderName: String,
    val senderAvatarUrl: String? = null,
    val senderBio: String? = null,
    val receiverId: String,
    val status: FriendRequestStatus = FriendRequestStatus.PENDING,
    val timestamp: Long = 0L
)
