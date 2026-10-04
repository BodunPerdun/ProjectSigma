package com.example.projectsigma.model

import kotlinx.serialization.Serializable

@Serializable
data class EventCluster(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val count: Int,
    val events: List<Event>
)
