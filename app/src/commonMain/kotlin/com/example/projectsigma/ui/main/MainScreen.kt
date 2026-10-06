package com.example.projectsigma.ui.main

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectsigma.data.FriendRequestService
import com.example.projectsigma.data.LocalFriendRequestServiceImpl
import com.example.projectsigma.data.LocalNotificationsRepositoryImpl
import com.example.projectsigma.data.NotificationsRepository
import com.example.projectsigma.i18n.AppLanguageManager
import com.example.projectsigma.model.EventCategory
import com.example.projectsigma.model.NotificationItem
import com.example.projectsigma.model.NotificationType
import com.example.projectsigma.model.User
import com.example.projectsigma.model.localizedLabel
import com.example.projectsigma.ui.components.UserAvatar
import com.example.projectsigma.viewmodel.AuthViewModel
import com.example.projectsigma.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    authViewModel: AuthViewModel,
    notificationsRepository: NotificationsRepository = remember { LocalNotificationsRepositoryImpl() },
    friendRequestService: FriendRequestService = remember(authViewModel, notificationsRepository) {
        LocalFriendRequestServiceImpl(authViewModel.authRepository, notificationsRepository)
    },
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    val clusters by mainViewModel.clusters.collectAsState()
    val selectedEvent by mainViewModel.selectedEvent.collectAsState()
    val selectedCluster by mainViewModel.selectedCluster.collectAsState()
    val selectedCategory by mainViewModel.selectedCategory.collectAsState()
    val editingEvent by mainViewModel.editingEvent.collectAsState()
    val isProfileOpen by mainViewModel.isProfileOpen.collectAsState()
    val isCreateEventOpen by mainViewModel.isCreateEventOpen.collectAsState()
    val newPinLocation by mainViewModel.newPinLocation.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    // Collect persistent notifications list from NotificationsRepository
    val rawNotifications by notificationsRepository.notifications.collectAsState()

    // Filter notifications so the current user ONLY sees notifications targeted to them
    val userNotifications = remember(rawNotifications, currentUser) {
        rawNotifications.filter {
            it.recipientUserId == null || it.recipientUserId == currentUser?.id
        }
    }

    // Collect current language state so all UI texts update instantly
    val currentLanguage by AppLanguageManager.currentLanguage.collectAsState()

    // System Window Insets for status bar & navigation bar padding
    val statusBarTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    var isFriendsOpen by remember { mutableStateOf(false) }
    var isSettingsOpen by remember { mutableStateOf(false) }
    var isNotificationsOpen by remember { mutableStateOf(false) }
    var myLocationTrigger by remember { mutableStateOf(0) }

    // Automated 1-Hour Pre-Event Starting Reminder Checker
    LaunchedEffect(clusters, currentUser) {
        val userEvents = clusters.flatMap { it.events }.filter { evt ->
            currentUser != null && (evt.participants.any { it.id == currentUser?.id } || evt.createdById == currentUser?.id)
        }

        userEvents.forEach { evt ->
            val reminderId = "reminder_1h_${evt.id}"
            val alreadySent = rawNotifications.any { it.id == reminderId }
            if (!alreadySent) {
                val notif = NotificationItem(
                    id = reminderId,
                    type = NotificationType.EVENT_REMINDER,
                    title = "⏰ Event Starting Soon",
                    message = "'${evt.title}' starts in 1 hour!",
                    timestampText = "Just now",
                    recipientUserId = currentUser?.id,
                    relatedEventId = evt.id
                )
                notificationsRepository.addNotification(notif)
            }
        }
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
                myLocationTrigger = myLocationTrigger,
                modifier = Modifier.fillMaxSize()
            )

            // Google Maps Style Floating Rounded Header Bar
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White.copy(alpha = 0.96f),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth(0.95f)
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
                        val hasUnread = userNotifications.any { !it.isRead }
                        Box {
                            Surface(
                                onClick = {
                                    isNotificationsOpen = true
                                    notificationsRepository.markAllAsRead()
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

            // Top Category Filter Chips
            key(currentLanguage) {
                Row(
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxWidth(0.95f)
                        .align(Alignment.TopCenter)
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

            // Bottom Right Floating Controls: My Location FAB & Friends FAB
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 24.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Floating My Location FAB Button (LocationOn Icon)
                Surface(
                    onClick = { myLocationTrigger++ },
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.96f),
                    shadowElevation = 8.dp,
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "My Location",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Friends FAB Button
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
                    notifications = userNotifications,
                    onAcceptFriendRequest = { notif ->
                        notif.senderUser?.let { sender ->
                            coroutineScope.launch {
                                currentUser?.let { curr ->
                                    friendRequestService.acceptFriendRequest(notif.id, sender.id, curr.id)
                                }
                            }
                        }
                        notificationsRepository.markAsHandled(notif.id)
                    },
                    onDeclineFriendRequest = { notif ->
                        coroutineScope.launch {
                            friendRequestService.declineFriendRequest(notif.id)
                        }
                        notificationsRepository.markAsHandled(notif.id)
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
                    allEvents = clusters.flatMap { it.events },
                    friendRequestService = friendRequestService,
                    onAddFriend = { targetUserId ->
                        if (currentUser != null && targetUserId != currentUser!!.id) {
                            val allParticipants = clusters.flatMap { it.events }.flatMap { it.participants }
                            val targetUser = allParticipants.find { it.id == targetUserId }
                                ?: authViewModel.getDiscoverableNearbyUsers().map { it.first }.find { it.id == targetUserId }
                                ?: User(id = targetUserId, email = "", displayName = "User", isSocialsPublic = true)

                            coroutineScope.launch {
                                friendRequestService.sendFriendRequest(currentUser!!, targetUser)
                            }
                        }
                    },
                    onRemoveFriend = { userId -> authViewModel.removeFriend(userId) },
                    onDismissRequest = { isFriendsOpen = false }
                )
            }

            if (isProfileOpen && currentUser != null) {
                ProfileBottomSheet(
                    user = currentUser!!,
                    userEvents = mainViewModel.getUserCreatedEvents(),
                    onEventClick = { eventId -> mainViewModel.onMarkerClick(eventId) },
                    onFriendsClick = { isFriendsOpen = true },
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
                    friendRequestService = friendRequestService,
                    onAddFriend = { targetUserId ->
                        if (currentUser != null && targetUserId != currentUser!!.id) {
                            val allParticipants = clusters.flatMap { it.events }.flatMap { it.participants }
                            val targetUser = allParticipants.find { it.id == targetUserId }
                                ?: authViewModel.getDiscoverableNearbyUsers().map { it.first }.find { it.id == targetUserId }
                                ?: User(id = targetUserId, email = "", displayName = "User", isSocialsPublic = true)

                            coroutineScope.launch {
                                friendRequestService.sendFriendRequest(currentUser!!, targetUser)
                            }
                        }
                    },
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
