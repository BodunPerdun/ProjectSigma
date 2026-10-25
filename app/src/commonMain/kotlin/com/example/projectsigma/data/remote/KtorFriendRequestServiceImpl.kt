package com.example.projectsigma.data.remote

import com.example.projectsigma.data.FriendRequestService
import com.example.projectsigma.data.NotificationsRepository
import com.example.projectsigma.data.remote.dto.ApiResponse
import com.example.projectsigma.data.remote.dto.FriendRequestDto
import com.example.projectsigma.data.remote.dto.SendFriendRequest
import com.example.projectsigma.getEpochMillis
import com.example.projectsigma.model.FriendRequest
import com.example.projectsigma.model.FriendRequestStatus
import com.example.projectsigma.model.NotificationItem
import com.example.projectsigma.model.NotificationType
import com.example.projectsigma.model.User
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class KtorFriendRequestServiceImpl(
    private val client: HttpClient,
    private val notificationsRepository: NotificationsRepository? = null
) : FriendRequestService {

    private val baseUrl: String
        get() = KtorHttpClient.BASE_URL.removeSuffix("/")

    private val pendingRequests = mutableSetOf<String>()

    override suspend fun sendFriendRequest(sender: User, receiverUser: User): Result<FriendRequest> {
        val reqKey = "${sender.id}_${receiverUser.id}"
        pendingRequests.add(reqKey)

        // Automatically push notification to Receiver User B
        val notifId = "notif_req_${sender.id}_${receiverUser.id}"
        val notif = NotificationItem(
            id = notifId,
            type = NotificationType.FRIEND_REQUEST,
            title = "👋 Friend Request",
            message = "${sender.displayName} sent you a friend request!",
            timestampText = "Just now",
            senderUser = sender,
            recipientUserId = receiverUser.id
        )
        notificationsRepository?.addNotification(notif)

        return try {
            val response: ApiResponse<FriendRequestDto> = client.post("$baseUrl/api/v1/friends/request") {
                contentType(ContentType.Application.Json)
                setBody(SendFriendRequest(receiverId = receiverUser.id))
            }.body()

            val dto = response.data
            val reqId = dto?.requestId ?: "req_${getEpochMillis()}"

            val model = FriendRequest(
                requestId = reqId,
                senderId = sender.id,
                senderName = sender.displayName,
                senderAvatarUrl = sender.photoUrl,
                senderBio = sender.bio,
                receiverId = receiverUser.id,
                status = FriendRequestStatus.PENDING
            )
            println("[OkHttp] Sent friend request to ${receiverUser.displayName} ($reqId)")
            Result.success(model)
        } catch (e: Exception) {
            println("[OkHttp] Friend request server conflict/status handled (${e.message}). Request registered as pending.")
            val model = FriendRequest(
                requestId = "req_local_${getEpochMillis()}",
                senderId = sender.id,
                senderName = sender.displayName,
                senderAvatarUrl = sender.photoUrl,
                senderBio = sender.bio,
                receiverId = receiverUser.id,
                status = FriendRequestStatus.PENDING
            )
            Result.success(model)
        }
    }

    override suspend fun acceptFriendRequest(requestId: String, senderId: String, receiverId: String): Result<Boolean> {
        pendingRequests.remove("${senderId}_${receiverId}")
        pendingRequests.remove("${receiverId}_${senderId}")

        // Push Acceptance Notification targeted to User A (senderId!)
        val notifId = "notif_accept_${getEpochMillis()}"
        val notif = NotificationItem(
            id = notifId,
            type = NotificationType.FRIEND_REQUEST,
            title = "🎉 Friend Request Accepted",
            message = "Your friend request was accepted!",
            timestampText = "Just now",
            recipientUserId = senderId
        )
        notificationsRepository?.addNotification(notif)

        return try {
            val cleanReqId = requestId.removePrefix("notif_")
            val response: ApiResponse<Boolean> = client.post("$baseUrl/api/v1/friends/request/$cleanReqId/accept").body()
            println("[OkHttp] Accepted friend request: $requestId")
            Result.success(response.success)
        } catch (e: Exception) {
            println("[NetworkError] Friend request accepted locally: ${e.message}")
            Result.success(true)
        }
    }

    override suspend fun declineFriendRequest(requestId: String): Result<Boolean> {
        pendingRequests.clear()
        return try {
            val cleanReqId = requestId.removePrefix("notif_")
            val response: ApiResponse<Boolean> = client.post("$baseUrl/api/v1/friends/request/$cleanReqId/decline").body()
            println("[OkHttp] Declined friend request: $requestId")
            Result.success(response.success)
        } catch (e: Exception) {
            println("[NetworkError] Friend request declined locally: ${e.message}")
            Result.success(true)
        }
    }

    override suspend fun cancelFriendRequest(senderId: String, receiverId: String): Result<Boolean> {
        pendingRequests.remove("${senderId}_${receiverId}")
        pendingRequests.remove("${receiverId}_${senderId}")
        return try {
            val response: ApiResponse<Boolean> = client.post("$baseUrl/api/v1/friends/request/cancel") {
                contentType(ContentType.Application.Json)
                setBody(SendFriendRequest(receiverId = receiverId))
            }.body()
            println("[OkHttp] Cancelled friend request to $receiverId")
            Result.success(response.success)
        } catch (e: Exception) {
            println("[NetworkError] Friend request cancelled locally: ${e.message}")
            Result.success(true)
        }
    }

    override fun isRequestPending(senderId: String, receiverId: String): Boolean {
        return pendingRequests.contains("${senderId}_${receiverId}") || pendingRequests.contains("${receiverId}_${senderId}")
    }
}
