package com.example.projectsigma.data

import com.example.projectsigma.getEpochMillis
import com.example.projectsigma.model.Event
import com.example.projectsigma.model.EventCategory
import com.example.projectsigma.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EventsRepository {

    private val initialEvents = listOf(
        Event(
            id = "evt_1",
            title = "Open Air Jazz Festival",
            description = "Enjoy live smooth jazz performance under the stars with food trucks and craft drinks.",
            category = EventCategory.PARTY,
            latitude = 51.5074,
            longitude = -0.1278,
            dateTime = "Today at 19:00",
            createdById = "user_10",
            createdByName = "London Music Club",
            createdByAvatarUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=150",
            createdAtTimestamp = getEpochMillis()
        ),
        Event(
            id = "evt_2",
            title = "Tech Startup Meetup & Pitch",
            description = "Network with co-founders, investors, and engineers. Pitching session starts at 18:30.",
            category = EventCategory.MEETUP,
            latitude = 51.5085,
            longitude = -0.1250,
            dateTime = "Tomorrow at 18:00",
            createdById = "user_demo_123",
            createdByName = "Alex Smith",
            createdAtTimestamp = getEpochMillis() - 3600000
        ),
        Event(
            id = "evt_3",
            title = "Street Food Night Market",
            description = "Over 20 artisanal food vendors showcasing world cuisine, desserts, and cocktails.",
            category = EventCategory.FOOD,
            latitude = 51.5090,
            longitude = -0.1280,
            dateTime = "Friday at 17:00",
            createdById = "user_12",
            createdByName = "Foodie Community",
            createdAtTimestamp = getEpochMillis() - 7200000
        ),
        Event(
            id = "evt_4",
            title = "Modern Art Gallery Exhibition",
            description = "Exclusive exhibition featuring contemporary digital art installations and sculpture.",
            category = EventCategory.CULTURE,
            latitude = 51.5060,
            longitude = -0.1295,
            dateTime = "Saturday at 12:00",
            createdById = "user_13",
            createdByName = "Metropolitan Art Space",
            createdAtTimestamp = getEpochMillis() - 10800000
        ),
        Event(
            id = "evt_5",
            title = "Morning Community Run & Yoga",
            description = "5km energetic park run followed by a 30-min outdoor yoga session. Free entry!",
            category = EventCategory.SPORTS,
            latitude = 51.5070,
            longitude = -0.1265,
            dateTime = "Sunday at 09:00",
            createdById = "user_14",
            createdByName = "Urban Runners",
            createdAtTimestamp = getEpochMillis() - 14400000
        ),
        Event(
            id = "evt_6",
            title = "Kotlin & AI Dev Workshop",
            description = "Hands-on session building KMP apps with AI agents. Bring your laptop!",
            category = EventCategory.EDUCATION,
            latitude = 51.5072,
            longitude = -0.1268,
            dateTime = "Next Monday at 15:00",
            createdById = "user_demo_123",
            createdByName = "Alex Smith",
            createdAtTimestamp = getEpochMillis() - 18000000
        )
    )

    private val _events = MutableStateFlow(initialEvents)
    val eventsFlow: StateFlow<List<Event>> = _events.asStateFlow()

    fun createEvent(
        title: String,
        description: String,
        category: EventCategory,
        latitude: Double,
        longitude: Double,
        dateTime: String,
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
            createdById = user.id,
            createdByName = user.displayName,
            createdByAvatarUrl = user.photoUrl,
            createdAtTimestamp = getEpochMillis()
        )
        _events.value = listOf(newEvent) + _events.value
        return newEvent
    }

    fun getEventsByUser(userId: String): List<Event> {
        return _events.value.filter { it.createdById == userId }
    }
}
