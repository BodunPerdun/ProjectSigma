package com.example.projectsigma.data.remote

import com.example.projectsigma.data.FriendRequestService
import com.example.projectsigma.data.remote.dto.ApiResponse
import com.example.projectsigma.data.remote.dto.FriendRequestDto
import com.example.projectsigma.data.remote.dto.SendFriendRequest
import com.example.projectsigma.getEpochMillis
import com.example.projectsigma.model.FriendRequest
import com.example.projectsigma.model.FriendRequestStatus
import com.example.projectsigma.model.User
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class KtorFriendRequestServiceImpl(
    private val client: HttpClient
) : FriendRequestService {

    private val baseUrl: String
        get() = KtorHttpClient.BASE_URL.removeSuffix("/")

    private val pendingRequests = mutableSetOf<String>()

    override suspend fun sendFriendRequest(sender: User, receiverUser: User): Result<FriendRequest> {
        pendingRequests.add("${sender.id}_${receiverUser.id}")
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
            println("[NetworkError] Friend request sent locally: ${e.message}")
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

    override fun isRequestPending(senderId: String, receiverId: String): Boolean {
        return pendingRequests.contains("${senderId}_${receiverId}") || pendingRequests.contains("${receiverId}_${senderId}")
    }
}
