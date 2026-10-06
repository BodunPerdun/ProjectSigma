package com.example.projectsigma.ui.main

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectsigma.i18n.AppLanguageManager
import com.example.projectsigma.model.EventCategory
import com.example.projectsigma.model.NotificationItem
import com.example.projectsigma.model.NotificationType
import com.example.projectsigma.model.User
import com.example.projectsigma.model.localizedLabel
import com.example.projectsigma.ui.components.UserAvatar
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

    // Collect current language state so all UI texts update instantly
    val currentLanguage by AppLanguageManager.currentLanguage.collectAsState()

    var isFriendsOpen by remember { mutableStateOf(false) }
    var isSettingsOpen by remember { mutableStateOf(false) }
    var isNotificationsOpen by remember { mutableStateOf(false) }

    val sampleSender = remember {
        User(
            id = "usr_near_1",
            email = "elena@example.com",
            displayName = "Elena Rostova",
            photoUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
            bio = "Software engineer & jazz enthusiast in London 🎷☕"
        )
    }

    var notifications by remember {
        mutableStateOf(
            listOf(
                NotificationItem(
                    id = "notif_1",
                    type = NotificationType.FRIEND_REQUEST,
                    title = "Friend Request",
                    message = "Elena Rostova sent you a friend request",
                    timestampText = "10m ago",
                    senderUser = sampleSender
                ),
                NotificationItem(
                    id = "notif_2",
                    type = NotificationType.EVENT_REMINDER,
                    title = "⏰ Event Starting Soon",
                    message = "'Open Air Jazz Festival' starts in 30 minutes!",
                    timestampText = "30m ago",
                    relatedEventId = "evt_1"
                )
            )
        )
    }

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

            // Google Maps Style Floating Rounded Header Bar
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White.copy(alpha = 0.96f),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp, start = 12.dp, end = 12.dp)
                    .height(56.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Minimalist Vector UI Settings Circle Button
                    Surface(
                        onClick = { isSettingsOpen = true },
                        shape = CircleShape,
                        color = Color(0xFFF0F4F8),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Center: Logo & Brand Name (LocaPop)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "LocaPop Logo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LocaPop",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    // Right Controls Group: 🔔 Notifications & Profile Avatar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Bell Notifications Circle Button with Unread Red Badge Dot
                        val hasUnread = notifications.any { !it.isRead }
                        Box {
                            Surface(
                                onClick = {
                                    isNotificationsOpen = true
                                    notifications = notifications.map { it.copy(isRead = true) }
                                },
                                shape = CircleShape,
                                color = Color(0xFFF0F4F8),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Notifications",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            if (hasUnread) {
                                Surface(
                                    color = MaterialTheme.colorScheme.error,
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .size(10.dp)
                                        .align(Alignment.TopEnd)
                                ) {}
                            }
                        }

                        // Profile Avatar Circle Button
                        Surface(
                            onClick = { mainViewModel.openProfile() },
                            shape = CircleShape,
                            color = Color(0xFFF0F4F8),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                UserAvatar(
                                    user = currentUser,
                                    size = 38.dp,
                                    textSizeSp = 15
                                )
                            }
                        }
                    }
                }
            }

            // Top Category Filter Chips with explicit key(currentLanguage) for 100% instant recomposition
            key(currentLanguage) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart)
                        .padding(top = 76.dp, start = 12.dp, end = 12.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { mainViewModel.selectCategory(null) },
                        label = { Text(AppLanguageManager.strings.catAll) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.White.copy(alpha = 0.92f)
                        )
                    )

                    EventCategory.entries.forEach { cat ->
                        val selected = selectedCategory == cat
                        FilterChip(
                            selected = selected,
                            onClick = { mainViewModel.selectCategory(cat) },
                            label = { Text("${cat.iconName} ${cat.localizedLabel}") },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.White.copy(alpha = 0.92f)
                            )
                        )
                    }
                }
            }

            // Bottom Right Floating Button: Vector Person UI Icon for Friends & Discovery
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 24.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                Surface(
                    onClick = { isFriendsOpen = true },
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.95f),
                    shadowElevation = 8.dp,
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Friends",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            // Bottom Sheets
            if (isNotificationsOpen) {
                NotificationsBottomSheet(
                    notifications = notifications,
                    onAcceptFriendRequest = { notif ->
                        notif.senderUser?.let { sender ->
                            authViewModel.addFriend(sender.id)
                        }
                        notifications = notifications.map {
                            if (it.id == notif.id) it.copy(isHandled = true, isRead = true) else it
                        }
                    },
                    onDeclineFriendRequest = { notif ->
                        notifications = notifications.map {
                            if (it.id == notif.id) it.copy(isHandled = true, isRead = true) else it
                        }
                    },
                    onNotificationClick = { notif ->
                        if (notif.relatedEventId != null) {
                            mainViewModel.onMarkerClick(notif.relatedEventId)
                            isNotificationsOpen = false
                        }
                    },
                    onDismissRequest = { isNotificationsOpen = false }
                )
            }

            if (isSettingsOpen) {
                SettingsBottomSheet(
                    onDismissRequest = { isSettingsOpen = false }
                )
            }

            if (isFriendsOpen && currentUser != null) {
                FriendsBottomSheet(
                    authViewModel = authViewModel,
                    currentUser = currentUser!!,
                    onDismissRequest = { isFriendsOpen = false }
                )
            }

            if (isProfileOpen && currentUser != null) {
                ProfileBottomSheet(
                    user = currentUser!!,
                    userEvents = mainViewModel.getUserCreatedEvents(),
                    onEventClick = { eventId -> mainViewModel.onMarkerClick(eventId) },
                    onLocationVisibilityChanged = { isVisible ->
                        authViewModel.updateLocationVisibility(isVisible)
                    },
                    onSocialsPublicityChanged = { isPublic ->
                        authViewModel.updateSocialsPublicity(isPublic)
                    },
                    onAvatarPhotoPicked = { photoUrl ->
                        authViewModel.updateProfilePhoto(photoUrl)
                        currentUser?.let { user ->
                            mainViewModel.updateUserAvatarInEvents(user.id, photoUrl, user.displayName)
                        }
                    },
                    onSocialHandlesUpdated = { insta, tg ->
                        authViewModel.updateSocialHandles(insta, tg)
                    },
                    onBioUpdated = { bio ->
                        authViewModel.updateUserBio(bio)
                    },
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
                    currentUser = currentUser,
                    onAddFriend = { userId -> authViewModel.addFriend(userId) },
                    onRemoveFriend = { userId -> authViewModel.removeFriend(userId) },
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
