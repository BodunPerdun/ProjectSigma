package com.example.projectsigma.data

import com.example.projectsigma.model.NotificationItem
import com.example.projectsigma.model.NotificationType
import com.example.projectsigma.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocalNotificationsRepositoryImpl : NotificationsRepository {

    private val defaultSampleNotifications = listOf(
        NotificationItem(
            id = "freq_usr_near_1_user_test_account_1",
            type = NotificationType.FRIEND_REQUEST,
            title = "Friend Request",
            message = "Elena Rostova sent you a friend request",
            timestampText = "10m ago",
            senderUser = User(
                id = "usr_near_1",
                email = "elena@example.com",
                displayName = "Elena Rostova",
                photoUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
                bio = "Software engineer & jazz enthusiast in London 🎷☕"
            ),
            recipientUserId = "user_test_account_1",
            isRead = false,
            isHandled = false
        ),
        NotificationItem(
            id = "notif_2",
            type = NotificationType.EVENT_REMINDER,
            title = "⏰ Event Starting Soon",
            message = "'Open Air Jazz Festival' starts in 1 hour!",
            timestampText = "30m ago",
            recipientUserId = "user_test_account_1",
            isRead = false,
            isHandled = false
        )
    )

    private val _notifications = MutableStateFlow<List<NotificationItem>>(defaultSampleNotifications)
    override val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    override fun addNotification(notification: NotificationItem) {
        _notifications.value = listOf(notification) + _notifications.value.filter { it.id != notification.id }
    }

    override fun markAllAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    override fun markAsHandled(id: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isHandled = true, isRead = true) else it
        }
    }
}
