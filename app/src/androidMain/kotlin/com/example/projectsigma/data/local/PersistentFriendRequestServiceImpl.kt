package com.example.projectsigma.data.local

import android.content.Context
import android.util.Log
import com.example.projectsigma.data.AuthRepository
import com.example.projectsigma.data.FriendRequestService
import com.example.projectsigma.data.NotificationsRepository
import com.example.projectsigma.model.FriendRequest
import com.example.projectsigma.model.FriendRequestStatus
import com.example.projectsigma.model.NotificationItem
import com.example.projectsigma.model.NotificationType
import com.example.projectsigma.model.User
import com.example.projectsigma.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class PersistentFriendRequestServiceImpl(
    private val context: Context,
    private val authRepository: AuthRepository,
    private val notificationsRepository: NotificationsRepository
) : FriendRequestService {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val requestsFile: File by lazy {
        File(context.filesDir, "persistent_friend_requests.json")
    }

    private val scope = CoroutineScope(Dispatchers.IO)
    private var requestsMap = mutableMapOf<String, FriendRequest>()

    init {
        loadFromDisk()
    }

    private fun loadFromDisk() {
        try {
            if (requestsFile.exists()) {
                val content = requestsFile.readText()
                if (content.isNotBlank()) {
                    val loaded = json.decodeFromString<List<FriendRequest>>(content)
                    requestsMap = loaded.associateBy { it.requestId }.toMutableMap()
                    Log.d("PersistentReqService", "Loaded ${loaded.size} friend requests from disk.")
                }
            }
        } catch (e: Exception) {
            Log.e("PersistentReqService", "Error loading requests: ${e.message}")
        }
    }

    private fun saveToDisk() {
        scope.launch {
            try {
                val content = json.encodeToString(requestsMap.values.toList())
                requestsFile.writeText(content)
            } catch (e: Exception) {
                Log.e("PersistentReqService", "Error saving requests: ${e.message}")
            }
        }
    }

    override suspend fun sendFriendRequest(sender: User, receiverUser: User): Result<FriendRequest> {
        if (sender.id == receiverUser.id) {
            return Result.failure(IllegalArgumentException("Cannot send friend request to yourself."))
        }

        val reqId = "freq_${sender.id}_${receiverUser.id}"
        val request = FriendRequest(
            requestId = reqId,
            senderId = sender.id,
            senderName = sender.displayName,
            senderAvatarUrl = sender.photoUrl,
            senderBio = sender.bio,
            receiverId = receiverUser.id,
            status = FriendRequestStatus.PENDING,
            timestamp = System.currentTimeMillis()
        )

        requestsMap[reqId] = request
        saveToDisk()

        // Send In-App Notification Item specifically targeting recipient user (receiverUser.id)
        val notif = NotificationItem(
            id = reqId,
            type = NotificationType.FRIEND_REQUEST,
            title = "Friend Request",
            message = "${sender.displayName} sent you a friend request",
            timestampText = "Just now",
            senderUser = sender,
            recipientUserId = receiverUser.id,
            isRead = false,
            isHandled = false
        )
        notificationsRepository.addNotification(notif)

        // Post Real Android Device Notification Bar Alert
        NotificationHelper.sendDeviceNotification(
            context = context,
            title = "👥 Friend Request",
            message = "${sender.displayName} sent you a friend request on LocaPop!"
        )

        Log.d("PersistentReqService", "Sent friend request from ${sender.displayName} to ${receiverUser.displayName}")
        return Result.success(request)
    }

    override suspend fun acceptFriendRequest(requestId: String, senderId: String, receiverId: String): Result<Boolean> {
        val cleanReqId = requestId.removePrefix("notif_")
        val existing = requestsMap[cleanReqId]
        if (existing != null) {
            requestsMap[cleanReqId] = existing.copy(status = FriendRequestStatus.ACCEPTED)
            saveToDisk()
        }

        // Add sender as friend for receiver
        authRepository.addFriend(senderId)

        return Result.success(true)
    }

    override suspend fun declineFriendRequest(requestId: String): Result<Boolean> {
        val cleanReqId = requestId.removePrefix("notif_")
        val existing = requestsMap[cleanReqId]
        if (existing != null) {
            requestsMap[cleanReqId] = existing.copy(status = FriendRequestStatus.DECLINED)
            saveToDisk()
        }
        return Result.success(true)
    }

    override fun isRequestPending(senderId: String, receiverId: String): Boolean {
        val reqId1 = "freq_${senderId}_${receiverId}"
        val reqId2 = "freq_${receiverId}_${senderId}"
        val req1 = requestsMap[reqId1]
        val req2 = requestsMap[reqId2]
        return (req1?.status == FriendRequestStatus.PENDING) || (req2?.status == FriendRequestStatus.PENDING)
    }
}
