package com.example.projectsigma.data.remote

import com.example.projectsigma.data.NotificationsRepository
import com.example.projectsigma.data.remote.dto.ApiResponse
import com.example.projectsigma.data.remote.dto.NotificationDto
import com.example.projectsigma.model.NotificationItem
import com.example.projectsigma.model.NotificationType
import com.example.projectsigma.model.User
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class KtorNotificationsRepositoryImpl(
    private val client: HttpClient,
    private val tokenManager: TokenManager? = null
) : NotificationsRepository {

    private val baseUrl: String
        get() = KtorHttpClient.BASE_URL.removeSuffix("/")

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    override val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private fun NotificationDto.toModel(): NotificationItem {
        val nType = if (type == "EVENT_REMINDER") NotificationType.EVENT_REMINDER else NotificationType.FRIEND_REQUEST
        return NotificationItem(
            id = id,
            type = nType,
            title = title,
            message = message,
            timestampText = timestampText,
            senderUser = senderUser?.let {
                User(
                    id = it.id,
                    email = "",
                    displayName = it.displayName,
                    photoUrl = it.photoUrl,
                    bio = it.bio
                )
            },
            recipientUserId = recipientUserId,
            relatedEventId = relatedEventId,
            isRead = isRead,
            isHandled = isHandled
        )
    }

    suspend fun fetchNotifications(): Result<List<NotificationItem>> {
        val token = tokenManager?.getAccessToken()
        if (token.isNullOrBlank()) {
            return Result.failure(Exception("No access token. Skipping protected notifications request."))
        }
        return try {
            val response: ApiResponse<List<NotificationDto>> = client.get("$baseUrl/api/v1/notifications").body()
            val dtoList = response.data ?: emptyList()
            val models = dtoList.map { it.toModel() }
            _notifications.value = models
            Result.success(models)
        } catch (e: Exception) {
            println("[NetworkError] Failed to fetch notifications from Ktor: ${e.message}")
            Result.failure(e)
        }
    }

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
