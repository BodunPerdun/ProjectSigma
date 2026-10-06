package com.example.projectsigma.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class NotificationDto(
    val id: String,
    val type: String,
    val title: String,
    val message: String,
    val timestampText: String,
    val senderUser: UserSummaryDto? = null,
    val recipientUserId: String? = null,
    val relatedEventId: String? = null,
    val isRead: Boolean = false,
    val isHandled: Boolean = false
)

@Serializable
data class MarkNotificationsReadRequest(
    val notificationIds: List<String> = emptyList()
)
