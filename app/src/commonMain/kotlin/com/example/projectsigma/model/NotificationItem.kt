package com.example.projectsigma.model

import kotlinx.serialization.Serializable

enum class NotificationType {
    FRIEND_REQUEST,
    EVENT_REMINDER
}

@Serializable
data class NotificationItem(
    val id: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val timestampText: String,
    val senderUser: User? = null,
    val relatedEventId: String? = null,
    val isRead: Boolean = false,
    val isHandled: Boolean = false
)
