package com.example.projectsigma.model

import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val id: String,
    val title: String,
    val description: String,
    val category: EventCategory,
    val latitude: Double,
    val longitude: Double,
    val dateTime: String,
    val createdById: String,
    val createdByName: String,
    val createdByAvatarUrl: String? = null,
    val createdAtTimestamp: Long = 0L
)
