package com.example.projectsigma.data.remote

import com.example.projectsigma.data.EventsRepository
import com.example.projectsigma.data.remote.dto.ApiResponse
import com.example.projectsigma.data.remote.dto.CreateEventRequest
import com.example.projectsigma.data.remote.dto.EventDto
import com.example.projectsigma.data.remote.dto.EventParticipantDto
import com.example.projectsigma.data.remote.dto.MapWebSocketMessage
import com.example.projectsigma.model.Event
import com.example.projectsigma.model.EventCategory
import com.example.projectsigma.model.User
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
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
    private val client: HttpClient,
    private val webSocketClient: MapWebSocketClient,
    private val tokenManager: TokenManager? = null
) : EventsRepository {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val baseUrl: String
        get() = KtorHttpClient.BASE_URL.removeSuffix("/")

    private val _eventsFlow = MutableStateFlow<List<Event>>(emptyList())
    override val eventsFlow: StateFlow<List<Event>> = _eventsFlow.asStateFlow()

    init {
        // Listen to WebSocket real-time map updates
        scope.launch {
            webSocketClient.messages.collect { wsMsg ->
                handleWebSocketMessage(wsMsg)
            }
        }

        // Fetch initial event pins from Ktor server
        scope.launch {
            fetchEventsInRegion()
        }
    }

    private fun handleWebSocketMessage(msg: MapWebSocketMessage) {
        when (msg.action) {
            "EVENT_CREATED" -> {
                msg.event?.toModel()?.let { newEvt ->
                    if (!_eventsFlow.value.any { it.id == newEvt.id }) {
                        _eventsFlow.value = _eventsFlow.value + newEvt
                    }
                }
            }
            "EVENT_UPDATED" -> {
                msg.event?.toModel()?.let { updatedEvt ->
                    _eventsFlow.value = _eventsFlow.value.map {
                        if (it.id == updatedEvt.id) updatedEvt else it
                    }
                }
            }
            "EVENT_DELETED" -> {
                val deletedId = msg.eventId ?: msg.event?.id
                if (!deletedId.isNullOrBlank()) {
                    _eventsFlow.value = _eventsFlow.value.filter { it.id != deletedId }
                }
            }
            "PARTICIPANT_JOINED" -> {
                val eventId = msg.eventId
                val joinedUser = msg.participant?.toModel() ?: msg.user?.toModel()
                if (!eventId.isNullOrBlank() && joinedUser != null) {
                    _eventsFlow.value = _eventsFlow.value.map { evt ->
                        if (evt.id == eventId && !evt.participants.any { it.id == joinedUser.id }) {
                            evt.copy(participants = evt.participants + joinedUser)
                        } else evt
                    }
                }
            }
            "PARTICIPANT_LEFT" -> {
                val eventId = msg.eventId
                val leftUserId = msg.userId
                if (!eventId.isNullOrBlank() && !leftUserId.isNullOrBlank()) {
                    _eventsFlow.value = _eventsFlow.value.map { evt ->
                        if (evt.id == eventId) {
                            evt.copy(participants = evt.participants.filter { it.id != leftUserId })
                        } else evt
                    }
                }
            }
            else -> {
                scope.launch { fetchEventsInRegion() }
            }
        }
    }

    private fun EventParticipantDto.toModel(): User {
        return User(
            id = validId,
            email = "",
            displayName = displayName,
            photoUrl = photoUrl,
            bio = bio,
            instagramHandle = instagramHandle,
            telegramHandle = telegramHandle,
            isSocialsPublic = isSocialsPublic
        )
    }

    private fun EventDto.toModel(): Event {
        val mappedCategory = try {
            EventCategory.valueOf(category.uppercase())
        } catch (e: Exception) {
            EventCategory.MEETUP
        }

        val creatorUser = createdBy
        return Event(
            id = if (id.isNotBlank()) id else "evt_server_${dateTime.hashCode()}",
            title = title,
            description = description,
            category = mappedCategory,
            latitude = latitude,
            longitude = longitude,
            dateTime = dateTime,
            createdById = creatorUser?.id ?: "usr_creator",
            createdByName = creatorUser?.displayName ?: "Organizer",
            createdByAvatarUrl = creatorUser?.photoUrl,
            photoUrl = photoUrl,
            participants = participants.map { it.toModel() }
        )
    }

    private fun mergeWithLocalCreatedEvents(serverEvents: List<Event>) {
        val localCreatedEvents = _eventsFlow.value.filter { it.id.startsWith("evt_local_") }
        if (localCreatedEvents.isEmpty()) {
            _eventsFlow.value = serverEvents
        } else {
            val serverMap = serverEvents.associateBy { it.id }
            val mergedList = serverEvents.toMutableList()
            localCreatedEvents.forEach { localEvt ->
                if (!serverMap.containsKey(localEvt.id)) {
                    mergedList.add(localEvt)
                }
            }
            _eventsFlow.value = mergedList
        }
    }

    suspend fun fetchEventsInRegion(): Result<List<Event>> {
        return try {
            val token = tokenManager?.getAccessToken()
            val response: ApiResponse<List<EventDto>> = client.get("$baseUrl/api/v1/events") {
                if (!token.isNullOrBlank()) {
                    header("Authorization", "Bearer $token")
                }
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
                val token = tokenManager?.getAccessToken()
                val request = CreateEventRequest(
                    title = title,
                    description = description,
                    category = category.name,
                    latitude = latitude,
                    longitude = longitude,
                    dateTime = dateTime,
                    photoUrl = photoUrl
                )
                val httpResponse: HttpResponse = client.post("$baseUrl/api/v1/events") {
                    contentType(ContentType.Application.Json)
                    if (!token.isNullOrBlank()) {
                        header("Authorization", "Bearer $token")
                    }
                    setBody(request)
                }

                if (httpResponse.status.value in 200..299) {
                    val response: ApiResponse<EventDto> = httpResponse.body()
                    val serverDto = response.data
                    if (serverDto != null) {
                        val serverModel = serverDto.toModel()
                        _eventsFlow.value = _eventsFlow.value.map {
                            if (it.id == localId) serverModel else it
                        }
                        println("[OkHttp] Confirmed event creation on Ktor server (${httpResponse.status}): ${serverModel.id}")
                    }
                } else {
                    val errorText = httpResponse.bodyAsText()
                    println("[NetworkError] Create event failed (${httpResponse.status}): $errorText")
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
                val token = tokenManager?.getAccessToken()
                val currentEvt = updatedList.find { it.id == eventId }
                val request = CreateEventRequest(
                    title = title,
                    description = description,
                    category = category.name,
                    latitude = currentEvt?.latitude ?: 0.0,
                    longitude = currentEvt?.longitude ?: 0.0,
                    dateTime = dateTime,
                    photoUrl = photoUrl
                )
                val httpResponse: HttpResponse = client.put("$baseUrl/api/v1/events/$eventId") {
                    contentType(ContentType.Application.Json)
                    if (!token.isNullOrBlank()) {
                        header("Authorization", "Bearer $token")
                    }
                    setBody(request)
                }
                if (httpResponse.status.value in 200..299) {
                    println("[OkHttp] Updated event $eventId on Ktor server (${httpResponse.status}).")
                } else {
                    println("[NetworkError] Update event failed (${httpResponse.status}): ${httpResponse.bodyAsText()}")
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
                val token = tokenManager?.getAccessToken()
                val httpResponse: HttpResponse = client.delete("$baseUrl/api/v1/events/$eventId") {
                    if (!token.isNullOrBlank()) {
                        header("Authorization", "Bearer $token")
                    }
                }
                if (httpResponse.status.value in 200..299) {
                    println("[OkHttp] Deleted event $eventId on Ktor server (${httpResponse.status}).")
                } else {
                    println("[NetworkError] Delete event failed (${httpResponse.status}): ${httpResponse.bodyAsText()}")
                }
                fetchEventsInRegion()
            } catch (e: Exception) {
                println("[NetworkError] Failed to delete event on Ktor: ${e.message}")
            }
        }

        return updated.size < originalSize
    }

    override fun purgeExpiredEvents() {
        // Handled by server or background sync
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
                val token = tokenManager?.getAccessToken()
                println("[OkHttp] Sending joinEvent request to server: $baseUrl/api/v1/events/$eventId/join (Token present: ${!token.isNullOrBlank()})")
                val httpResponse: HttpResponse = client.post("$baseUrl/api/v1/events/$eventId/join") {
                    if (!token.isNullOrBlank()) {
                        header("Authorization", "Bearer $token")
                    }
                }
                if (httpResponse.status.value in 200..299) {
                    println("[OkHttp] Successfully joined event $eventId on Ktor server (${httpResponse.status}).")
                } else {
                    println("[NetworkError] Join event failed (${httpResponse.status}): ${httpResponse.bodyAsText()}")
                }
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
                val token = tokenManager?.getAccessToken()
                println("[OkHttp] Sending leaveEvent request to server: $baseUrl/api/v1/events/$eventId/leave (Token present: ${!token.isNullOrBlank()})")
                val httpResponse: HttpResponse = client.post("$baseUrl/api/v1/events/$eventId/leave") {
                    if (!token.isNullOrBlank()) {
                        header("Authorization", "Bearer $token")
                    }
                }
                if (httpResponse.status.value in 200..299) {
                    println("[OkHttp] Successfully left event $eventId on Ktor server (${httpResponse.status}).")
                } else {
                    println("[NetworkError] Leave event failed (${httpResponse.status}): ${httpResponse.bodyAsText()}")
                }
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
