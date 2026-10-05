package com.example.projectsigma.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val categoryName: String,
    val latitude: Double,
    val longitude: Double,
    val dateTime: String,
    val photoUrl: String?,
    val createdById: String,
    val createdByName: String,
    val createdByAvatarUrl: String?,
    val createdAtTimestamp: Long,
    val participantsJson: String,
    val isSynced: Boolean = false,
    val syncStatus: String = "PENDING_PUSH" // Sync statuses: "SYNCED", "PENDING_PUSH", "PENDING_UPDATE", "PENDING_DELETE"
)
