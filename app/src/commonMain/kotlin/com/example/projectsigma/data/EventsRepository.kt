package com.example.projectsigma.data

import com.example.projectsigma.model.Event
import com.example.projectsigma.model.EventCategory
import com.example.projectsigma.model.User
import kotlinx.coroutines.flow.StateFlow

interface EventsRepository {
    val eventsFlow: StateFlow<List<Event>>
    fun createEvent(
        title: String,
        description: String,
        category: EventCategory,
        latitude: Double,
        longitude: Double,
        dateTime: String,
        photoUrl: String?,
        user: User
    ): Event
    fun updateEvent(
        eventId: String,
        title: String,
        description: String,
        category: EventCategory,
        dateTime: String,
        photoUrl: String?
    ): Event?
    fun deleteEvent(eventId: String): Boolean
    fun purgeExpiredEvents()
    fun joinEvent(eventId: String, user: User): Event?
    fun leaveEvent(eventId: String, userId: String): Event?
    fun updateUserAvatarInEvents(userId: String, newPhotoUrl: String?, newDisplayName: String)
    fun getEventsByUser(userId: String): List<Event>
}
