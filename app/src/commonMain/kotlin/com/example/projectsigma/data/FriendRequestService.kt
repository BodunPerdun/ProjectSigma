package com.example.projectsigma.data

import com.example.projectsigma.model.FriendRequest
import com.example.projectsigma.model.User

/**
 * Server-ready abstraction interface for sending and managing friend requests.
 * Easy to replace with a real backend server (Ktor, Firebase, REST/WebSocket) later!
 */
interface FriendRequestService {
    suspend fun sendFriendRequest(sender: User, receiverUser: User): Result<FriendRequest>
    suspend fun acceptFriendRequest(requestId: String, senderId: String, receiverId: String): Result<Boolean>
    suspend fun declineFriendRequest(requestId: String): Result<Boolean>
    fun isRequestPending(senderId: String, receiverId: String): Boolean
}
