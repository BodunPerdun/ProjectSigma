package com.example.projectsigma.data

import com.example.projectsigma.model.NotificationItem
import kotlinx.coroutines.flow.StateFlow

interface NotificationsRepository {
    val notifications: StateFlow<List<NotificationItem>>
    fun addNotification(notification: NotificationItem)
    fun markAllAsRead()
    fun markAsHandled(id: String)
}
