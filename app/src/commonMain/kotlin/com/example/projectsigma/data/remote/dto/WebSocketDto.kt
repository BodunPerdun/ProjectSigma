package com.example.projectsigma.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MapWebSocketMessage(
    val action: String, // "EVENT_CREATED", "EVENT_UPDATED", "EVENT_DELETED", "PARTICIPANT_JOINED", "PARTICIPANT_LEFT"
    val event: EventDto? = null,
    val eventId: String? = null,
    val userId: String? = null
)
