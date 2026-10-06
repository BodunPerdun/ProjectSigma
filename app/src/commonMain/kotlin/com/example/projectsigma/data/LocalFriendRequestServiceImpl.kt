package com.example.projectsigma.data

import com.example.projectsigma.model.FriendRequest
import com.example.projectsigma.model.FriendRequestStatus
import com.example.projectsigma.model.NotificationItem
import com.example.projectsigma.model.NotificationType
import com.example.projectsigma.model.User

class LocalFriendRequestServiceImpl(
    private val authRepository: AuthRepository,
    private val notificationsRepository: NotificationsRepository
) : FriendRequestService {

    private val requestsMap = mutableMapOf<String, FriendRequest>()

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
            timestamp = 0L
        )

        requestsMap[reqId] = request

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

        return Result.success(request)
    }

    override suspend fun acceptFriendRequest(requestId: String, senderId: String, receiverId: String): Result<Boolean> {
        val cleanReqId = requestId.removePrefix("notif_")
        val existing = requestsMap[cleanReqId]
        if (existing != null) {
            requestsMap[cleanReqId] = existing.copy(status = FriendRequestStatus.ACCEPTED)
        }
        authRepository.addFriend(senderId)
        return Result.success(true)
    }

    override suspend fun declineFriendRequest(requestId: String): Result<Boolean> {
        val cleanReqId = requestId.removePrefix("notif_")
        val existing = requestsMap[cleanReqId]
        if (existing != null) {
            requestsMap[cleanReqId] = existing.copy(status = FriendRequestStatus.DECLINED)
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
