package com.example.projectsigma.data.remote

import com.example.projectsigma.data.FriendRequestService
import com.example.projectsigma.data.remote.dto.ApiResponse
import com.example.projectsigma.data.remote.dto.FriendRequestDto
import com.example.projectsigma.data.remote.dto.SendFriendRequest
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

    private val pendingRequests = mutableSetOf<String>()

    override suspend fun sendFriendRequest(sender: User, receiverUser: User): Result<FriendRequest> {
        return try {
            val response: ApiResponse<FriendRequestDto> = client.post("${KtorHttpClient.BASE_URL}/api/v1/friends/request") {
                contentType(ContentType.Application.Json)
                setBody(SendFriendRequest(receiverId = receiverUser.id))
            }.body()

            val dto = response.data
            if (response.success && dto != null) {
                val reqId = dto.requestId
                pendingRequests.add("${sender.id}_${receiverUser.id}")

                val model = FriendRequest(
                    requestId = reqId,
                    senderId = sender.id,
                    senderName = sender.displayName,
                    senderAvatarUrl = sender.photoUrl,
                    senderBio = sender.bio,
                    receiverId = receiverUser.id,
                    status = FriendRequestStatus.PENDING
                )
                Result.success(model)
            } else {
                Result.failure(Exception(response.message ?: "Failed to send friend request."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun acceptFriendRequest(requestId: String, senderId: String, receiverId: String): Result<Boolean> {
        return try {
            val cleanReqId = requestId.removePrefix("notif_")
            val response: ApiResponse<Boolean> = client.post("${KtorHttpClient.BASE_URL}/api/v1/friends/request/$cleanReqId/accept").body()
            Result.success(response.success)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun declineFriendRequest(requestId: String): Result<Boolean> {
        return try {
            val cleanReqId = requestId.removePrefix("notif_")
            val response: ApiResponse<Boolean> = client.post("${KtorHttpClient.BASE_URL}/api/v1/friends/request/$cleanReqId/decline").body()
            Result.success(response.success)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isRequestPending(senderId: String, receiverId: String): Boolean {
        return pendingRequests.contains("${senderId}_${receiverId}") || pendingRequests.contains("${receiverId}_${senderId}")
    }
}
