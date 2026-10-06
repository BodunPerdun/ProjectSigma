package com.example.projectsigma.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SendFriendRequest(
    val receiverId: String
)

@Serializable
data class FriendRequestDto(
    val requestId: String,
    val sender: UserSummaryDto,
    val receiverId: String,
    val status: String,
    val createdAt: String? = null
)
