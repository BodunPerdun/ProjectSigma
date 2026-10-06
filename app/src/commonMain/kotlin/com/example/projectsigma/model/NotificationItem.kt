package com.example.projectsigma.model

import com.example.projectsigma.i18n.AppLanguage
import com.example.projectsigma.i18n.AppLanguageManager
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
    val recipientUserId: String? = null,
    val relatedEventId: String? = null,
    val isRead: Boolean = false,
    val isHandled: Boolean = false
)

val NotificationItem.localizedTitle: String
    get() = when (type) {
        NotificationType.FRIEND_REQUEST -> when (AppLanguageManager.currentLanguage.value) {
            AppLanguage.RUSSIAN -> "Запрос в друзья"
            AppLanguage.UKRAINIAN -> "Запит у друзі"
            AppLanguage.POLISH -> "Zaproszenie do znajomych"
            AppLanguage.ENGLISH -> "Friend Request"
        }
        NotificationType.EVENT_REMINDER -> when (AppLanguageManager.currentLanguage.value) {
            AppLanguage.RUSSIAN -> "⏰ Событие скоро начнется"
            AppLanguage.UKRAINIAN -> "⏰ Подія скоро розпочнеться"
            AppLanguage.POLISH -> "⏰ Wydarzenie wkrótce się розпочнеться"
            AppLanguage.ENGLISH -> "⏰ Event Starting Soon"
        }
    }

val NotificationItem.localizedMessage: String
    get() = when (type) {
        NotificationType.FRIEND_REQUEST -> {
            val name = senderUser?.displayName ?: "Someone"
            when (AppLanguageManager.currentLanguage.value) {
                AppLanguage.RUSSIAN -> "$name отправил вам запрос в друзья"
                AppLanguage.UKRAINIAN -> "$name надіслав вам запит у друзі"
                AppLanguage.POLISH -> "$name wysłał Ci zaproszenie do znajomych"
                AppLanguage.ENGLISH -> "$name sent you a friend request"
            }
        }
        NotificationType.EVENT_REMINDER -> message
    }
