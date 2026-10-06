package com.example.projectsigma.data.local

import android.content.Context
import android.util.Log
import com.example.projectsigma.data.EventsRepository
import com.example.projectsigma.getEpochMillis
import com.example.projectsigma.model.Event
import com.example.projectsigma.model.EventCategory
import com.example.projectsigma.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RoomEventsRepositoryImpl(context: Context) : EventsRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val db = AppDatabase.getDatabase(context)
    private val eventDao = db.eventDao()
    private val scope = CoroutineScope(Dispatchers.IO)

    private val sampleUser1 = User("usr_alex", "alex@example.com", "Alex Smith", eventsCount = 3)
    private val sampleUser2 = User("usr_maria", "maria@example.com", "Maria Garcia", eventsCount = 1)
    private val sampleUser3 = User("usr_david", "david@example.com", "David Chen", eventsCount = 4)

    private val sampleEvents = listOf(
        Event(
            id = "evt_1",
            title = "Open Air Jazz Festival",
            description = "Enjoy live smooth jazz performance under the stars with food trucks and craft drinks.",
            category = EventCategory.PARTY,
            latitude = 51.5074,
            longitude = -0.1278,
            dateTime = "25.10.2026 19:00 - 22:00",
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
            dateTime = "26.10.2026 18:00 - 20:30",
            photoUrl = "https://images.unsplash.com/photo-1515187029135-18ee286d815b?w=600",
            createdById = "user_test_account_1",
            createdByName = "Test User",
            createdByAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
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
            dateTime = "28.10.2026 17:00 - 21:00",
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
            dateTime = "30.10.2026 12:00 - 16:00",
            photoUrl = "https://images.unsplash.com/photo-1536924940846-227afb31e2a5?w=600",
            createdById = "user_13",
            createdByName = "Metropolitan Art Space",
            participants = listOf(sampleUser3),
            createdAtTimestamp = getEpochMillis() - 10800000
        )
    )

    private val _events = MutableStateFlow<List<Event>>(sampleEvents)
    override val eventsFlow: StateFlow<List<Event>> = _events.asStateFlow()

    init {
        scope.launch {
            try {
                eventDao.getAllEvents().collect { entities ->
                    if (entities.isEmpty()) {
                        // Populate initial Room database with sample events
                        val initialEntities = sampleEvents.map { it.toEntity(isSynced = true, status = "SYNCED") }
                        eventDao.insertEvents(initialEntities)
                    } else {
                        _events.value = entities.map { it.toDomainModel() }
                    }
                }
            } catch (e: Exception) {
                Log.e("RoomEventsRepo", "Error collecting Room events: ${e.message}")
            }
        }
    }

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

        scope.launch {
            try {
                eventDao.insertEvent(newEvent.toEntity(isSynced = false, status = "PENDING_PUSH"))
            } catch (e: Exception) {
                Log.e("RoomEventsRepo", "Error inserting event: ${e.message}")
            }
        }

        val currentList = _events.value
        _events.value = listOf(newEvent) + currentList
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

            scope.launch {
                try {
                    eventDao.insertEvent(updatedEvent.toEntity(isSynced = false, status = "PENDING_UPDATE"))
                } catch (e: Exception) {
                    Log.e("RoomEventsRepo", "Error updating event: ${e.message}")
                }
            }

            return updatedEvent
        }
        return null
    }

    override fun deleteEvent(eventId: String): Boolean {
        val currentList = _events.value.toMutableList()
        val removed = currentList.removeAll { it.id == eventId }
        if (removed) {
            _events.value = currentList
            scope.launch {
                try {
                    eventDao.deleteEvent(eventId)
                } catch (e: Exception) {
                    Log.e("RoomEventsRepo", "Error deleting event: ${e.message}")
                }
            }
        }
        return removed
    }

    override fun purgeExpiredEvents() {
        val expiredEvents = _events.value.filter { isEventExpired(it.dateTime) }
        if (expiredEvents.isNotEmpty()) {
            val expiredIds = expiredEvents.map { it.id }
            val freshList = _events.value.filterNot { expiredIds.contains(it.id) }
            _events.value = freshList

            scope.launch {
                try {
                    eventDao.deleteEventsByIds(expiredIds)
                } catch (e: Exception) {
                    Log.e("RoomEventsRepo", "Error purging expired events: ${e.message}")
                }
            }
        }
    }

    override fun updateUserAvatarInEvents(userId: String, newPhotoUrl: String?, newDisplayName: String) {
        val currentList = _events.value.map { event ->
            val updatedParticipants = event.participants.map { participant ->
                if (participant.id == userId) {
                    participant.copy(photoUrl = newPhotoUrl, displayName = newDisplayName)
                } else participant
            }
            if (event.createdById == userId) {
                event.copy(
                    createdByAvatarUrl = newPhotoUrl,
                    createdByName = newDisplayName,
                    participants = updatedParticipants
                )
            } else {
                event.copy(participants = updatedParticipants)
            }
        }
        _events.value = currentList
        scope.launch {
            try {
                eventDao.insertEvents(currentList.map { it.toEntity(isSynced = false, status = "PENDING_UPDATE") })
            } catch (e: Exception) {
                Log.e("RoomEventsRepo", "Error updating user avatar in Room: ${e.message}")
            }
        }
    }

    private fun isEventExpired(dateTimeStr: String): Boolean {
        return try {
            val datePart = dateTimeStr.substringBefore(" ").trim()
            val timePart = dateTimeStr.substringAfter(" ").trim()
            val endStr = if (timePart.contains("-")) timePart.substringAfter("-").trim() else timePart

            val dateComponents = datePart.split(".")
            if (dateComponents.size < 3) return false
            val day = dateComponents[0].toInt()
            val month = dateComponents[1].toInt()
            val year = dateComponents[2].toInt()

            val timeComponents = endStr.split(":")
            val endHour = timeComponents[0].toInt()
            val endMin = timeComponents.getOrNull(1)?.toInt() ?: 0

            val ldt = LocalDateTime(year, month, day, endHour, endMin)
            val endInstant = ldt.toInstant(TimeZone.currentSystemDefault())
            val nowInstant = Clock.System.now()

            endInstant < nowInstant
        } catch (_: Exception) {
            false
        }
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

                scope.launch {
                    try {
                        eventDao.insertEvent(updatedEvent.toEntity(isSynced = false, status = "PENDING_UPDATE"))
                    } catch (e: Exception) {
                        Log.e("RoomEventsRepo", "Error joining event: ${e.message}")
                    }
                }

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

            scope.launch {
                try {
                    eventDao.insertEvent(updatedEvent.toEntity(isSynced = false, status = "PENDING_UPDATE"))
                } catch (e: Exception) {
                    Log.e("RoomEventsRepo", "Error leaving event: ${e.message}")
                }
            }

            return updatedEvent
        }
        return null
    }

    override fun getEventsByUser(userId: String): List<Event> {
        return _events.value.filter { it.createdById == userId }
    }

    private fun Event.toEntity(isSynced: Boolean, status: String): EventEntity {
        val serializedParticipants = try {
            json.encodeToString(participants)
        } catch (_: Exception) {
            "[]"
        }

        return EventEntity(
            id = id,
            title = title,
            description = description,
            categoryName = category.name,
            latitude = latitude,
            longitude = longitude,
            dateTime = dateTime,
            photoUrl = photoUrl,
            createdById = createdById,
            createdByName = createdByName,
            createdByAvatarUrl = createdByAvatarUrl,
            createdAtTimestamp = createdAtTimestamp,
            participantsJson = serializedParticipants,
            isSynced = isSynced,
            syncStatus = status
        )
    }

    private fun EventEntity.toDomainModel(): Event {
        val cat = try {
            EventCategory.valueOf(categoryName)
        } catch (_: Exception) {
            EventCategory.MEETUP
        }

        val parsedParticipants = try {
            json.decodeFromString<List<User>>(participantsJson)
        } catch (_: Exception) {
            emptyList()
        }

        return Event(
            id = id,
            title = title,
            description = description,
            category = cat,
            latitude = latitude,
            longitude = longitude,
            dateTime = dateTime,
            photoUrl = photoUrl,
            createdById = createdById,
            createdByName = createdByName,
            createdByAvatarUrl = createdByAvatarUrl,
            participants = parsedParticipants,
            createdAtTimestamp = createdAtTimestamp
        )
    }
}
