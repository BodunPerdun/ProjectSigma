package com.example.projectsigma.data

import com.example.projectsigma.getEpochMillis
import com.example.projectsigma.model.Event
import com.example.projectsigma.model.EventCategory
import com.example.projectsigma.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocalEventsRepositoryImpl : EventsRepository {

    private val sampleUser1 = User("usr_alex", "alex@example.com", "Alex Smith", eventsCount = 3)
    private val sampleUser2 = User("usr_maria", "maria@example.com", "Maria Garcia", eventsCount = 1)
    private val sampleUser3 = User("usr_david", "david@example.com", "David Chen", eventsCount = 4)

    private val initialEvents = listOf(
        Event(
            id = "evt_1",
            title = "Open Air Jazz Festival",
            description = "Enjoy live smooth jazz performance under the stars with food trucks and craft drinks.",
            category = EventCategory.PARTY,
            latitude = 51.5074,
            longitude = -0.1278,
            dateTime = "05.10.2026 19:00 - 22:00",
            photoUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600",
            createdById = "user_10",
            createdByName = "London Music Club",
            createdByAvatarUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=150",
            participants = listOf(sampleUser1, sampleUser2),
            createdAtTimestamp = getEpochMillis()
        ),
        Event(
            id = "evt_2",
            title = "Tech Startup Meetup & Pitch",
            description = "Network with co-founders, investors, and engineers. Pitching session starts at 18:30.",
            category = EventCategory.MEETUP,
            latitude = 51.5085,
            longitude = -0.1250,
            dateTime = "06.10.2026 18:00 - 20:30",
            photoUrl = "https://images.unsplash.com/photo-1515187029135-18ee286d815b?w=600",
            createdById = "user_test_account_1",
            createdByName = "Test User",
            participants = listOf(sampleUser1, sampleUser3),
            createdAtTimestamp = getEpochMillis() - 3600000
        ),
        Event(
            id = "evt_3",
            title = "Street Food Night Market",
            description = "Over 20 artisanal food vendors showcasing world cuisine, desserts, and cocktails.",
            category = EventCategory.FOOD,
            latitude = 51.5090,
            longitude = -0.1280,
            dateTime = "08.10.2026 17:00 - 21:00",
            photoUrl = "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=600",
            createdById = "user_12",
            createdByName = "Foodie Community",
            participants = listOf(sampleUser2),
            createdAtTimestamp = getEpochMillis() - 7200000
        ),
        Event(
            id = "evt_4",
            title = "Modern Art Gallery Exhibition",
            description = "Exclusive exhibition featuring contemporary digital art installations and sculpture.",
            category = EventCategory.CULTURE,
            latitude = 51.5060,
            longitude = -0.1295,
            dateTime = "10.10.2026 12:00 - 16:00",
            photoUrl = "https://images.unsplash.com/photo-1536924940846-227afb31e2a5?w=600",
            createdById = "user_13",
            createdByName = "Metropolitan Art Space",
            participants = listOf(sampleUser3),
            createdAtTimestamp = getEpochMillis() - 10800000
        )
    )

    private val _events = MutableStateFlow(initialEvents)
    override val eventsFlow: StateFlow<List<Event>> = _events.asStateFlow()

    override fun createEvent(
        title: String,
        description: String,
        category: EventCategory,
        latitude: Double,
        longitude: Double,
        dateTime: String,
        photoUrl: String?,
        user: User
    ): Event {
        val newEvent = Event(
            id = "evt_${getEpochMillis()}",
            title = title,
            description = description,
            category = category,
            latitude = latitude,
            longitude = longitude,
            dateTime = dateTime,
            photoUrl = photoUrl,
            createdById = user.id,
            createdByName = user.displayName,
            createdByAvatarUrl = user.photoUrl,
            participants = listOf(user),
            createdAtTimestamp = getEpochMillis()
        )
        _events.value = listOf(newEvent) + _events.value
        return newEvent
    }

    override fun updateEvent(
        eventId: String,
        title: String,
        description: String,
        category: EventCategory,
        dateTime: String,
        photoUrl: String?
    ): Event? {
        val currentList = _events.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == eventId }
        if (index != -1) {
            val oldEvent = currentList[index]
            val updatedEvent = oldEvent.copy(
                title = title,
                description = description,
                category = category,
                dateTime = dateTime,
                photoUrl = photoUrl
            )
            currentList[index] = updatedEvent
            _events.value = currentList
            return updatedEvent
        }
        return null
    }

    override fun joinEvent(eventId: String, user: User): Event? {
        val currentList = _events.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == eventId }
        if (index != -1) {
            val event = currentList[index]
            if (event.participants.none { it.id == user.id }) {
                val updatedParticipants = event.participants + user
                val updatedEvent = event.copy(participants = updatedParticipants)
                currentList[index] = updatedEvent
                _events.value = currentList
                return updatedEvent
            }
        }
        return null
    }

    override fun leaveEvent(eventId: String, userId: String): Event? {
        val currentList = _events.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == eventId }
        if (index != -1) {
            val event = currentList[index]
            val updatedParticipants = event.participants.filter { it.id != userId }
            val updatedEvent = event.copy(participants = updatedParticipants)
            currentList[index] = updatedEvent
            _events.value = currentList
            return updatedEvent
        }
        return null
    }

    override fun getEventsByUser(userId: String): List<Event> {
        return _events.value.filter { it.createdById == userId }
    }
}
