package com.example.projectsigma.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectsigma.data.FriendRequestService
import com.example.projectsigma.i18n.AppLanguageManager
import com.example.projectsigma.model.Event
import com.example.projectsigma.model.User
import com.example.projectsigma.model.localizedTitle
import com.example.projectsigma.ui.components.UserAvatar
import com.example.projectsigma.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsBottomSheet(
    authViewModel: AuthViewModel,
    currentUser: User,
    allEvents: List<Event> = emptyList(),
    friendRequestService: FriendRequestService? = null,
    onAddFriend: (userId: String) -> Unit = {},
    onRemoveFriend: (userId: String) -> Unit = {},
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val uriHandler = LocalUriHandler.current

    val currentLanguage by AppLanguageManager.currentLanguage.collectAsState()
    val s = AppLanguageManager.strings

    var selectedTabIndex by remember { mutableStateOf(0) }
    var selectedUserProfile by remember { mutableStateOf<User?>(null) }

    val friendUsers = remember(currentUser.friends, allEvents, currentLanguage) {
        val allParticipants = allEvents.flatMap { it.participants }
        authViewModel.getFriendUsers(allParticipants)
    }

    // Compute co-attendees from events currentUser attended or created
    val coAttendees = remember(currentUser, allEvents, currentLanguage) {
        val userEvents = allEvents.filter { evt ->
            evt.createdById == currentUser.id || evt.participants.any { it.id == currentUser.id }
        }
        val participantsMap = mutableMapOf<String, Pair<User, String>>() // userId -> (User, Shared Event Title)
        userEvents.forEach { evt ->
            evt.participants.filter { it.id != currentUser.id }.forEach { p ->
                if (!participantsMap.containsKey(p.id)) {
                    participantsMap[p.id] = Pair(p, evt.localizedTitle)
                }
            }
        }
        // Fallback to discoverable nearby users if user hasn't joined events with others yet
        if (participantsMap.isEmpty()) {
            authViewModel.getDiscoverableNearbyUsers().map { Pair(it.first, it.second) }
        } else {
            participantsMap.values.toList()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = s.friendsSheetTitle,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Tabs
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Text(
                            text = "${s.myFriendsTab} (${friendUsers.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Text(
                            text = s.eventCoAttendeesTab,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTabIndex) {
                0 -> {
                    // TAB 1: My Friends List
                    if (friendUsers.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(s.noFriendsYetMsg, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                s.noFriendsSubtitle,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(friendUsers) { friend ->
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedUserProfile = friend
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        UserAvatar(
                                            user = friend,
                                            size = 44.dp,
                                            textSizeSp = 18
                                        )

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = friend.displayName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = s.tapToViewProfile,
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
                                        }

                                        OutlinedButton(
                                            onClick = { onRemoveFriend(friend.id) },
                                            shape = RoundedCornerShape(16.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = MaterialTheme.colorScheme.error
                                            )
                                        ) {
                                            Text(s.removeFriendBtn, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 2: Event Co-Attendees (People from events user attended)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = s.eventCoAttendeesSubtitle,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (coAttendees.isEmpty()) {
                            Text(
                                text = s.noCoAttendeesMsg,
                                fontSize = 13.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(coAttendees) { (coAttendeeUser, eventOrDistanceInfo) ->
                                    val isFriend = currentUser.friends.contains(coAttendeeUser.id)
                                    val isPending = friendRequestService?.isRequestPending(currentUser.id, coAttendeeUser.id) == true

                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedUserProfile = coAttendeeUser
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            UserAvatar(
                                                user = coAttendeeUser,
                                                size = 44.dp,
                                                textSizeSp = 18
                                            )

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = coAttendeeUser.displayName,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Surface(
                                                    color = Color(0xFFE3F2FD),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text(
                                                        text = "🎉 $eventOrDistanceInfo",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF1976D2),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            if (isFriend) {
                                                Surface(
                                                    color = Color(0xFFE8F5E9),
                                                    shape = RoundedCornerShape(14.dp)
                                                ) {
                                                    Text(
                                                        text = s.friendBadge,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF2E7D32),
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                    )
                                                }
                                            } else if (isPending) {
                                                Surface(
                                                    color = Color(0xFFFFF3CD),
                                                    shape = RoundedCornerShape(14.dp)
                                                ) {
                                                    Text(
                                                        text = s.pendingRequestBtn,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF856404),
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                                    )
                                                }
                                            } else {
                                                Button(
                                                    onClick = { onAddFriend(coAttendeeUser.id) },
                                                    shape = RoundedCornerShape(16.dp)
                                                ) {
                                                    Text(s.addFriendBtn, fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Sheet: Friend / Event Co-Attendee Full Profile Card
    selectedUserProfile?.let { targetUser ->
        val userSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val isAlreadyFriend = currentUser.friends.contains(targetUser.id)
        val isPending = friendRequestService?.isRequestPending(currentUser.id, targetUser.id) == true

        ModalBottomSheet(
            onDismissRequest = { selectedUserProfile = null },
            sheetState = userSheetState,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                UserAvatar(
                    user = targetUser,
                    size = 80.dp,
                    textSizeSp = 36
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = targetUser.displayName,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                // Display User Bio if present
                if (!targetUser.bio.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = Color(0xFFF8F9FA),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = s.bioTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = targetUser.bio,
                                fontSize = 12.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Social Media Deep Link Chips (Privacy-Aware: Checks targetUser.isSocialsPublic)
                if (targetUser.isSocialsPublic) {
                    val hasSocials = !targetUser.instagramHandle.isNullOrBlank() || !targetUser.telegramHandle.isNullOrBlank()
                    if (hasSocials) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!targetUser.instagramHandle.isNullOrBlank()) {
                                Surface(
                                    color = Color(0xFFFCE4EC),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.clickable {
                                        uriHandler.openUri("https://instagram.com/_u/${targetUser.instagramHandle}")
                                    }
                                ) {
                                    Text(
                                        text = "📸 Instagram",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFC2185B),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }

                            if (!targetUser.telegramHandle.isNullOrBlank()) {
                                Surface(
                                    color = Color(0xFFE3F2FD),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.clickable {
                                        uriHandler.openUri("https://t.me/${targetUser.telegramHandle}")
                                    }
                                ) {
                                    Text(
                                        text = "✈️ Telegram",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1976D2),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Text(s.noSocialsMsg, fontSize = 12.sp, color = Color.Gray)
                    }
                } else {
                    Surface(
                        color = Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = s.socialsPrivateMsg,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${targetUser.eventsCount}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = s.eventsAttended,
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = s.accountStatusActive,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                            Text(
                                text = s.accountStatusTitle,
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (isAlreadyFriend) {
                        OutlinedButton(
                            onClick = {
                                onRemoveFriend(targetUser.id)
                                selectedUserProfile = null
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(s.removeFriendBtn, fontWeight = FontWeight.Bold)
                        }
                    } else if (isPending) {
                        Button(
                            onClick = {},
                            enabled = false,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(
                                disabledContainerColor = Color(0xFFFFF3CD),
                                disabledContentColor = Color(0xFF856404)
                            )
                        ) {
                            Text(s.pendingRequestBtn, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    } else {
                        Button(
                            onClick = {
                                onAddFriend(targetUser.id)
                                selectedUserProfile = null
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(25.dp)
                        ) {
                            Text(s.addFriendBtn, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = { selectedUserProfile = null },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0))
                    ) {
                        Text(s.closeBtn, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
