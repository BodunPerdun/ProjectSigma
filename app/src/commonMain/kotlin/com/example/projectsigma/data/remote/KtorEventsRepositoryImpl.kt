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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class KtorEventsRepositoryImpl(
    private val client: HttpClient,
    private val webSocketClient: MapWebSocketClient? = null
) : EventsRepository {

    private val baseUrl: String
        get() = KtorHttpClient.BASE_URL.removeSuffix("/")

    private val scope = CoroutineScope(Dispatchers.IO)
    private val _eventsFlow = MutableStateFlow<List<Event>>(emptyList())
    override val eventsFlow: StateFlow<List<Event>> = _eventsFlow.asStateFlow()

    init {
        scope.launch {
            fetchEventsInRegion()
        }

        // Periodic 8-second auto-sync timer to ensure maps on all devices stay synchronized
        scope.launch {
            while (isActive) {
                delay(8.seconds)
                fetchEventsInRegion()
            }
        }

        webSocketClient?.let { ws ->
            scope.launch {
                ws.connect()
                ws.messages.collect { wsMessage ->
                    println("[OkHttp] Received WebSocket event action: ${wsMessage.action}")
                    when (wsMessage.action) {
                        "EVENT_CREATED" -> {
                            wsMessage.event?.let { dto ->
                                val newModel = dto.toModel()
                                if (!_eventsFlow.value.any { it.id == newModel.id }) {
                                    _eventsFlow.value = _eventsFlow.value + newModel
                                    println("[OkHttp] WebSocket dynamically added new event pin: ${newModel.title}")
                                }
                            } ?: fetchEventsInRegion()
                        }
                        "EVENT_UPDATED" -> {
                            wsMessage.event?.let { dto ->
                                val updatedModel = dto.toModel()
                                _eventsFlow.value = _eventsFlow.value.map {
                                    if (it.id == updatedModel.id) updatedModel else it
                                }
                            } ?: fetchEventsInRegion()
                        }
                        "EVENT_DELETED" -> {
                            wsMessage.eventId?.let { delId ->
                                _eventsFlow.value = _eventsFlow.value.filter { it.id != delId }
                            } ?: fetchEventsInRegion()
                        }
                        "PARTICIPANT_JOINED", "PARTICIPANT_LEFT" -> {
                            fetchEventsInRegion()
                        }
                        else -> {
                            fetchEventsInRegion()
                        }
                    }
                }
            }
        }
    }

    private fun EventDto.toModel(): Event {
        val creatorId = createdBy?.validId ?: "user_creator"
        val creatorName = createdBy?.displayName ?: "Organizer"
        val creatorAvatar = createdBy?.photoUrl

        return Event(
            id = id,
            title = title,
            description = description,
            category = EventCategory.fromName(category),
            latitude = latitude,
            longitude = longitude,
            dateTime = dateTime,
            createdById = creatorId,
            createdByName = creatorName,
            createdByAvatarUrl = creatorAvatar,
            photoUrl = photoUrl,
            participants = participants.map { p ->
                User(
                    id = p.validId,
                    email = "",
                    displayName = p.displayName,
                    photoUrl = p.photoUrl,
                    bio = p.bio
                )
            }
        )
    }

    private fun mergeWithLocalCreatedEvents(serverEvents: List<Event>) {
        val serverIds = serverEvents.map { it.id }.toSet()
        val localCreated = _eventsFlow.value.filter { localEvt ->
            localEvt.id.startsWith("evt_local_") && !serverIds.contains(localEvt.id)
        }
        _eventsFlow.value = (serverEvents + localCreated).distinctBy { it.id }
    }

    suspend fun fetchEventsInRegion(
        minLat: Double? = null,
        maxLat: Double? = null,
        minLng: Double? = null,
        maxLng: Double? = null,
        category: String? = null
    ): Result<List<Event>> {
        return try {
            val response: ApiResponse<List<EventDto>> = client.get("$baseUrl/api/v1/events") {
                minLat?.let { parameter("minLat", it) }
                maxLat?.let { parameter("maxLat", it) }
                minLng?.let { parameter("minLng", it) }
                maxLng?.let { parameter("maxLng", it) }
                category?.let { parameter("category", it) }
            }.body()

            val dtoList = response.data ?: emptyList()
            val models = dtoList.map { it.toModel() }
            mergeWithLocalCreatedEvents(models)
            println("[OkHttp] Sync: Fetched ${models.size} events from Ktor server.")
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
        val localId = "evt_local_${user.id}_${dateTime.hashCode()}_${_eventsFlow.value.size}"
        val newEvt = Event(
            id = localId,
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
                val response: ApiResponse<EventDto> = client.post("$baseUrl/api/v1/events") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.body()

                val serverDto = response.data
                if (response.success && serverDto != null) {
                    val serverModel = serverDto.toModel()
                    _eventsFlow.value = _eventsFlow.value.map {
                        if (it.id == localId) serverModel else it
                    }
                    println("[OkHttp] Confirmed event on Ktor server: ${serverModel.id}")
                }
                fetchEventsInRegion()
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
                client.put("$baseUrl/api/v1/events/$eventId") {
                    contentType(ContentType.Application.Json)
                }
                fetchEventsInRegion()
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
                client.delete("$baseUrl/api/v1/events/$eventId")
                fetchEventsInRegion()
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
                client.post("$baseUrl/api/v1/events/$eventId/join")
                println("[OkHttp] Joined event $eventId on Ktor server.")
                fetchEventsInRegion()
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
                client.post("$baseUrl/api/v1/events/$eventId/leave")
                println("[OkHttp] Left event $eventId on Ktor server.")
                fetchEventsInRegion()
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
