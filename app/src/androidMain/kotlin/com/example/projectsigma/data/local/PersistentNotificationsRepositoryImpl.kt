package com.example.projectsigma.data.local

import android.content.Context
import android.util.Log
import com.example.projectsigma.data.NotificationsRepository
import com.example.projectsigma.model.NotificationItem
import com.example.projectsigma.model.NotificationType
import com.example.projectsigma.model.User
import com.example.projectsigma.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class PersistentNotificationsRepositoryImpl(private val context: Context) : NotificationsRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val notificationsFile: File by lazy {
        File(context.filesDir, "persistent_notifications.json")
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    override val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

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
            relatedEventId = "evt_1",
            isRead = false,
            isHandled = false
        )
    )

    init {
        loadFromDisk()
    }

    private fun loadFromDisk() {
        try {
            if (notificationsFile.exists()) {
                val content = notificationsFile.readText()
                if (content.isNotBlank()) {
                    val loaded = json.decodeFromString<List<NotificationItem>>(content)
                    _notifications.value = loaded
                    Log.d("PersistentNotifRepo", "Loaded ${loaded.size} notifications from disk.")
                    return
                }
            }
        } catch (e: Exception) {
            Log.e("PersistentNotifRepo", "Error loading notifications: ${e.message}")
        }

        _notifications.value = defaultSampleNotifications
        saveToDisk(defaultSampleNotifications)
    }

    private fun saveToDisk(items: List<NotificationItem>) {
        scope.launch {
            try {
                val content = json.encodeToString(items)
                notificationsFile.writeText(content)
                Log.d("PersistentNotifRepo", "Saved ${items.size} notifications to disk.")
            } catch (e: Exception) {
                Log.e("PersistentNotifRepo", "Error saving notifications: ${e.message}")
            }
        }
    }

    override fun addNotification(notification: NotificationItem) {
        val updated = listOf(notification) + _notifications.value.filter { it.id != notification.id }
        _notifications.value = updated
        saveToDisk(updated)

        // Post a REAL Android System Notification bar alert
        NotificationHelper.sendDeviceNotification(context, notification.title, notification.message)
    }

    override fun markAllAsRead() {
        val updated = _notifications.value.map { it.copy(isRead = true) }
        _notifications.value = updated
        saveToDisk(updated)
    }

    override fun markAsHandled(id: String) {
        val updated = _notifications.value.map {
            if (it.id == id) it.copy(isHandled = true, isRead = true) else it
        }
        _notifications.value = updated
        saveToDisk(updated)
    }
}
