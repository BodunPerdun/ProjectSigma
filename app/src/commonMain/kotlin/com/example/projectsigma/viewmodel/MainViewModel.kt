package com.example.projectsigma.viewmodel

import com.example.projectsigma.clustering.ClusterManager
import com.example.projectsigma.data.AuthRepository
import com.example.projectsigma.data.EventsRepository
import com.example.projectsigma.model.Event
import com.example.projectsigma.model.EventCategory
import com.example.projectsigma.model.EventCluster
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MainViewModel(
    private val eventsRepository: EventsRepository,
    private val authRepository: AuthRepository
) {

    private val scope = CoroutineScope(Dispatchers.Main)

    private val _selectedCategory = MutableStateFlow<EventCategory?>(null)
    val selectedCategory: StateFlow<EventCategory?> = _selectedCategory.asStateFlow()

    private val _currentZoom = MutableStateFlow(13)
    val currentZoom: StateFlow<Int> = _currentZoom.asStateFlow()

    private val _selectedEvent = MutableStateFlow<Event?>(null)
    val selectedEvent: StateFlow<Event?> = _selectedEvent.asStateFlow()

    private val _selectedCluster = MutableStateFlow<EventCluster?>(null)
    val selectedCluster: StateFlow<EventCluster?> = _selectedCluster.asStateFlow()

    private val _editingEvent = MutableStateFlow<Event?>(null)
    val editingEvent: StateFlow<Event?> = _editingEvent.asStateFlow()

    private val _isProfileOpen = MutableStateFlow(false)
    val isProfileOpen: StateFlow<Boolean> = _isProfileOpen.asStateFlow()

    private val _isCreateEventOpen = MutableStateFlow(false)
    val isCreateEventOpen: StateFlow<Boolean> = _isCreateEventOpen.asStateFlow()

    private val _newPinLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val newPinLocation: StateFlow<Pair<Double, Double>?> = _newPinLocation.asStateFlow()

    val rawEvents: StateFlow<List<Event>> = eventsRepository.eventsFlow

    private val _clusters = MutableStateFlow<List<EventCluster>>(emptyList())
    val clusters: StateFlow<List<EventCluster>> = _clusters.asStateFlow()

    init {
        scope.launch {
            combine(
                eventsRepository.eventsFlow,
                _selectedCategory,
                _currentZoom
            ) { allEvents, category, zoom ->
                val filtered = if (category == null) allEvents else allEvents.filter { it.category == category }
                ClusterManager.clusterEvents(filtered, zoom)
            }.collect { computedClusters ->
                _clusters.value = computedClusters
            }
        }
    }

    fun selectCategory(category: EventCategory?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    fun onZoomChanged(newZoom: Int) {
        _currentZoom.value = newZoom
    }

    fun onMarkerClick(eventId: String) {
        val event = rawEvents.value.find { it.id == eventId }
        if (event != null) {
            _selectedEvent.value = event
            _selectedCluster.value = null
            _isCreateEventOpen.value = false
            _isProfileOpen.value = false
        }
    }

    fun onClusterClick(cluster: EventCluster) {
        if (cluster.count == 1) {
            _selectedEvent.value = cluster.events.firstOrNull()
            _selectedCluster.value = null
        } else {
            _selectedCluster.value = cluster
            _selectedEvent.value = null
        }
    }

    fun onMapClick(lat: Double, lng: Double) {
        _selectedEvent.value = null
        _selectedCluster.value = null
        _editingEvent.value = null
        _newPinLocation.value = Pair(lat, lng)
        _isCreateEventOpen.value = true // Opens "Create event here" bottom sheet on tap
    }

    fun openCreateEventForm(lat: Double = 51.5074, lng: Double = -0.1278) {
        _editingEvent.value = null
        _newPinLocation.value = Pair(lat, lng)
        _isCreateEventOpen.value = true
        _selectedEvent.value = null
    }

    fun openEditEventForm(event: Event) {
        _editingEvent.value = event
        _newPinLocation.value = Pair(event.latitude, event.longitude)
        _isCreateEventOpen.value = true
    }

    fun closeCreateEventForm() {
        _isCreateEventOpen.value = false
        _editingEvent.value = null
        _newPinLocation.value = null
    }

    fun createEvent(
        title: String,
        description: String,
        category: EventCategory,
        dateTime: String,
        photoUrl: String? = null
    ) {
        val user = authRepository.currentUser.value ?: return
        val location = _newPinLocation.value ?: Pair(51.5074, -0.1278)

        val newEvent = eventsRepository.createEvent(
            title = title,
            description = description,
            category = category,
            latitude = location.first,
            longitude = location.second,
            dateTime = dateTime,
            photoUrl = photoUrl,
            user = user
        )

        _selectedEvent.value = newEvent

        val userEvents = eventsRepository.getEventsByUser(user.id)
        authRepository.updateUserEventCount(userEvents.size)

        closeCreateEventForm()
    }

    fun updateEvent(
        title: String,
        description: String,
        category: EventCategory,
        dateTime: String,
        photoUrl: String?
    ) {
        val eventToEdit = _editingEvent.value ?: return
        val updated = eventsRepository.updateEvent(
            eventId = eventToEdit.id,
            title = title,
            description = description,
            category = category,
            dateTime = dateTime,
            photoUrl = photoUrl
        )
        if (updated != null) {
            _selectedEvent.value = updated
        }
        _editingEvent.value = null
        _isCreateEventOpen.value = false
        _newPinLocation.value = null
    }

    fun joinEvent(eventId: String) {
        val user = authRepository.currentUser.value ?: return
        val updated = eventsRepository.joinEvent(eventId, user)
        if (updated != null) {
            _selectedEvent.value = updated
        }
    }

    fun leaveEvent(eventId: String) {
        val user = authRepository.currentUser.value ?: return
        val updated = eventsRepository.leaveEvent(eventId, user.id)
        if (updated != null) {
            _selectedEvent.value = updated
        }
    }

    fun openProfile() {
        _isProfileOpen.value = true
    }

    fun closeProfile() {
        _isProfileOpen.value = false
    }

    fun dismissEventDetails() {
        _selectedEvent.value = null
        _selectedCluster.value = null
    }

    fun getUserCreatedEvents(): List<Event> {
        val currentUserId = authRepository.currentUser.value?.id ?: return emptyList()
        return eventsRepository.getEventsByUser(currentUserId)
    }
}
