package com.example.projectsigma.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class EventParticipantDto(
    val id: String,
    val displayName: String,
    val photoUrl: String? = null,
    val bio: String? = null,
    val joinedAt: String? = null
)

@Serializable
data class EventDto(
    val id: String,
    val createdBy: UserSummaryDto,
    val title: String,
    val description: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val dateTime: String,
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
