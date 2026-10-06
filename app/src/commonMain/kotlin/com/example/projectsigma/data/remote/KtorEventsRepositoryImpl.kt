package com.example.projectsigma.data.remote

import com.example.projectsigma.data.EventsRepository
import com.example.projectsigma.data.remote.dto.ApiResponse
import com.example.projectsigma.data.remote.dto.CreateEventRequest
import com.example.projectsigma.data.remote.dto.EventDto
import com.example.projectsigma.model.Event
import com.example.projectsigma.model.EventCategory
import com.example.projectsigma.model.User
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class KtorEventsRepositoryImpl(
    private val client: HttpClient
) : EventsRepository {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val _eventsFlow = MutableStateFlow<List<Event>>(emptyList())
    override val eventsFlow: StateFlow<List<Event>> = _eventsFlow.asStateFlow()

    init {
        scope.launch {
            fetchEventsInRegion()
        }
    }

    private fun EventDto.toModel(): Event {
        return Event(
            id = id,
            title = title,
            description = description,
            category = EventCategory.fromName(category),
            latitude = latitude,
            longitude = longitude,
            dateTime = dateTime,
            createdById = createdBy.id,
            createdByName = createdBy.displayName,
            createdByAvatarUrl = createdBy.photoUrl,
            photoUrl = photoUrl,
            participants = participants.map {
                User(
                    id = it.id,
                    email = "",
                    displayName = it.displayName,
                    photoUrl = it.photoUrl,
                    bio = it.bio
                )
            }
        )
    }

    suspend fun fetchEventsInRegion(
        minLat: Double? = null,
        maxLat: Double? = null,
        minLng: Double? = null,
        maxLng: Double? = null,
        category: String? = null
    ): Result<List<Event>> {
        return try {
            val response: ApiResponse<List<EventDto>> = client.get("${KtorHttpClient.BASE_URL}api/v1/events") {
                minLat?.let { parameter("minLat", it) }
                maxLat?.let { parameter("maxLat", it) }
                minLng?.let { parameter("minLng", it) }
                maxLng?.let { parameter("maxLng", it) }
                category?.let { parameter("category", it) }
            }.body()

            val dtoList = response.data ?: emptyList()
            val models = dtoList.map { it.toModel() }
            _eventsFlow.value = models
            println("[OkHttp] Fetched ${models.size} events from Ktor server.")
            Result.success(models)
        } catch (e: Exception) {
            println("[NetworkError] Failed to fetch events from Ktor server: ${e.message}")
            Result.failure(e)
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
        val newEvt = Event(
            id = "evt_${user.id}_${dateTime.hashCode()}",
            title = title,
            description = description,
            category = category,
            latitude = latitude,
            longitude = longitude,
            dateTime = dateTime,
            createdById = user.id,
            createdByName = user.displayName,
            createdByAvatarUrl = user.photoUrl,
            photoUrl = photoUrl
        )
        _eventsFlow.value = _eventsFlow.value + newEvt

        // Post to remote Ktor backend in background
        scope.launch {
            try {
                val request = CreateEventRequest(
                    title = title,
                    description = description,
                    category = category.name,
                    latitude = latitude,
                    longitude = longitude,
                    dateTime = dateTime,
                    photoUrl = photoUrl
                )
                val response: ApiResponse<EventDto> = client.post("${KtorHttpClient.BASE_URL}api/v1/events") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.body()
                println("[OkHttp] Event created on Ktor server: ${response.data?.id}")
            } catch (e: Exception) {
                println("[NetworkError] Failed to post created event to Ktor: ${e.message}")
            }
        }

        return newEvt
    }

    override fun updateEvent(
        eventId: String,
        title: String,
        description: String,
        category: EventCategory,
        dateTime: String,
        photoUrl: String?
    ): Event? {
        val updatedList = _eventsFlow.value.map { evt ->
            if (evt.id == eventId) {
                evt.copy(
                    title = title,
                    description = description,
                    category = category,
                    dateTime = dateTime,
                    photoUrl = photoUrl
                )
            } else evt
        }
        _eventsFlow.value = updatedList

        scope.launch {
            try {
                client.put("${KtorHttpClient.BASE_URL}api/v1/events/$eventId") {
                    contentType(ContentType.Application.Json)
                }
            } catch (e: Exception) {
                println("[NetworkError] Failed to update event on Ktor: ${e.message}")
            }
        }

        return updatedList.find { it.id == eventId }
    }

    override fun deleteEvent(eventId: String): Boolean {
        val originalSize = _eventsFlow.value.size
        val updated = _eventsFlow.value.filter { it.id != eventId }
        _eventsFlow.value = updated

        scope.launch {
            try {
                client.delete("${KtorHttpClient.BASE_URL}api/v1/events/$eventId")
            } catch (e: Exception) {
                println("[NetworkError] Failed to delete event on Ktor: ${e.message}")
            }
        }

        return updated.size < originalSize
    }

    override fun purgeExpiredEvents() {
        // No-op for remote API or handled by server
    }

    override fun joinEvent(eventId: String, user: User): Event? {
        val updatedList = _eventsFlow.value.map { evt ->
            if (evt.id == eventId && !evt.participants.any { it.id == user.id }) {
                evt.copy(participants = evt.participants + user)
            } else evt
        }
        _eventsFlow.value = updatedList

        scope.launch {
            try {
                client.post("${KtorHttpClient.BASE_URL}api/v1/events/$eventId/join")
                println("[OkHttp] Joined event $eventId on Ktor server.")
            } catch (e: Exception) {
                println("[NetworkError] Failed to send joinEvent to Ktor: ${e.message}")
            }
        }

        return updatedList.find { it.id == eventId }
    }

    override fun leaveEvent(eventId: String, userId: String): Event? {
        val updatedList = _eventsFlow.value.map { evt ->
            if (evt.id == eventId) {
                evt.copy(participants = evt.participants.filter { it.id != userId })
            } else evt
        }
        _eventsFlow.value = updatedList

        scope.launch {
            try {
                client.post("${KtorHttpClient.BASE_URL}api/v1/events/$eventId/leave")
                println("[OkHttp] Left event $eventId on Ktor server.")
            } catch (e: Exception) {
                println("[NetworkError] Failed to send leaveEvent to Ktor: ${e.message}")
            }
        }

        return updatedList.find { it.id == eventId }
    }

    override fun getEventsByUser(userId: String): List<Event> {
        return _eventsFlow.value.filter { it.createdById == userId }
    }

    override fun updateUserAvatarInEvents(userId: String, newPhotoUrl: String?, newDisplayName: String) {
        _eventsFlow.value = _eventsFlow.value.map { evt ->
            val updatedCreatorAvatar = if (evt.createdById == userId) newPhotoUrl else evt.createdByAvatarUrl
            val updatedCreatorName = if (evt.createdById == userId) newDisplayName else evt.createdByName
            val updatedParticipants = evt.participants.map { p ->
                if (p.id == userId) {
                    p.copy(
                        photoUrl = newPhotoUrl,
                        displayName = newDisplayName
                    )
                } else p
            }
            evt.copy(
                createdByAvatarUrl = updatedCreatorAvatar,
                createdByName = updatedCreatorName,
                participants = updatedParticipants
            )
        }
    }
}
