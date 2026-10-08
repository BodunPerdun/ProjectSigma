package com.example.projectsigma.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class EventParticipantDto(
    val id: String = "",
    val userId: String? = null,
    val displayName: String = "Participant",
    val photoUrl: String? = null,
    val bio: String? = null,
    val joinedAt: String? = null
) {
    val validId: String
        get() = if (id.isNotBlank()) id else (userId ?: "user_anon")
}

@Serializable
data class EventDto(
    val id: String = "",
    val createdBy: UserSummaryDto? = null,
    val title: String = "",
    val description: String = "",
    val category: String = "OTHER",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val dateTime: String = "",
    val photoUrl: String? = null,
    val participantsCount: Int = 0,
    val participants: List<EventParticipantDto> = emptyList(),
    val isJoined: Boolean = false,
    val createdAt: String? = null
)

@Serializable
data class CreateEventRequest(
    val title: String,
    val description: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val dateTime: String,
    val photoUrl: String? = null
)

@Serializable
data class UpdateEventRequest(
    val title: String? = null,
    val description: String? = null,
    val category: String? = null,
    val dateTime: String? = null,
    val photoUrl: String? = null
)
