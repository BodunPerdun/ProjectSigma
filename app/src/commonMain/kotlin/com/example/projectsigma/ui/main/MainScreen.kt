package com.example.projectsigma.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectsigma.model.EventCategory
import com.example.projectsigma.viewmodel.AuthViewModel
import com.example.projectsigma.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val clusters by mainViewModel.clusters.collectAsState()
    val selectedEvent by mainViewModel.selectedEvent.collectAsState()
    val selectedCluster by mainViewModel.selectedCluster.collectAsState()
    val selectedCategory by mainViewModel.selectedCategory.collectAsState()
    val editingEvent by mainViewModel.editingEvent.collectAsState()
    val isProfileOpen by mainViewModel.isProfileOpen.collectAsState()
    val isCreateEventOpen by mainViewModel.isCreateEventOpen.collectAsState()
    val newPinLocation by mainViewModel.newPinLocation.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Full screen MapLibre GL Native Map view
            MapLibreMapContainer(
                clusters = clusters,
                selectedEvent = selectedEvent,
                onMarkerClick = { eventId -> mainViewModel.onMarkerClick(eventId) },
                onClusterClick = { cluster -> mainViewModel.onClusterClick(cluster) },
                onMapClick = { lat, lng -> mainViewModel.onMapClick(lat, lng) },
                onZoomChanged = { zoom -> mainViewModel.onZoomChanged(zoom) },
                modifier = Modifier.fillMaxSize()
            )

            // Top Header Bar & Category Filter Chips
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
                    .align(Alignment.TopCenter)
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🗺️", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SocialMap",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        // Top Right User Profile Avatar Button
                        IconButton(
                            onClick = { mainViewModel.openProfile() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentUser?.displayName?.take(1)?.uppercase() ?: "👤",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }

                // Category Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { mainViewModel.selectCategory(null) },
                        label = { Text("All Events") },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.White.copy(alpha = 0.9f)
                        )
                    )

                    EventCategory.entries.forEach { cat ->
                        val selected = selectedCategory == cat
                        FilterChip(
                            selected = selected,
                            onClick = { mainViewModel.selectCategory(cat) },
                            label = { Text("${cat.iconName} ${cat.label}") },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.White.copy(alpha = 0.9f)
                            )
                        )
                    }
                }
            }

            // Bottom Sheets
            if (isProfileOpen && currentUser != null) {
                ProfileBottomSheet(
                    user = currentUser!!,
                    userEvents = mainViewModel.getUserCreatedEvents(),
                    onEventClick = { eventId -> mainViewModel.onMarkerClick(eventId) },
                    onDismissRequest = { mainViewModel.closeProfile() },
                    onLogoutClick = { authViewModel.logout() }
                )
            }

            if (isCreateEventOpen && newPinLocation != null) {
                CreateEventBottomSheet(
                    location = newPinLocation!!,
                    eventToEdit = editingEvent,
                    onDismissRequest = { mainViewModel.closeCreateEventForm() },
                    onCreateEvent = { title, desc, cat, dateTime, photoUrl ->
                        if (editingEvent != null) {
                            mainViewModel.updateEvent(title, desc, cat, dateTime, photoUrl)
                        } else {
                            mainViewModel.createEvent(title, desc, cat, dateTime, photoUrl)
                        }
                    }
                )
            }

            selectedCluster?.let { cluster ->
                ClusterEventsBottomSheet(
                    cluster = cluster,
                    onEventClick = { eventId -> mainViewModel.onMarkerClick(eventId) },
                    onDismissRequest = { mainViewModel.dismissEventDetails() }
                )
            }

            selectedEvent?.let { event ->
                EventDetailsBottomSheet(
                    event = event,
                    currentUserId = currentUser?.id,
                    onJoinClick = { eventId -> mainViewModel.joinEvent(eventId) },
                    onLeaveClick = { eventId -> mainViewModel.leaveEvent(eventId) },
                    onEditClick = { evt -> mainViewModel.openEditEventForm(evt) },
                    onDeleteClick = { eventId -> mainViewModel.deleteEvent(eventId) },
                    onDismissRequest = { mainViewModel.dismissEventDetails() }
                )
            }
        }
    }
}
