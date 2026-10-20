package com.example.projectsigma.ui.main

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectsigma.data.FriendRequestService
import com.example.projectsigma.i18n.AppLanguageManager
import com.example.projectsigma.model.Event
import com.example.projectsigma.model.User
import com.example.projectsigma.model.formattedDateTime
import com.example.projectsigma.model.localizedDescription
import com.example.projectsigma.model.localizedLabel
import com.example.projectsigma.model.localizedTitle
import com.example.projectsigma.shareText
import com.example.projectsigma.ui.components.AsyncEventImage
import com.example.projectsigma.ui.components.UserAvatar
import com.example.projectsigma.ui.main.components.AllParticipantsSheet
import com.example.projectsigma.ui.main.components.ParticipantProfileSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsBottomSheet(
    event: Event,
    currentUser: User?,
    friendRequestService: FriendRequestService? = null,
    onAddFriend: (userId: String) -> Unit = {},
    onRemoveFriend: (userId: String) -> Unit = {},
    onJoinClick: (eventId: String) -> Unit,
    onLeaveClick: (eventId: String) -> Unit,
    onEditClick: (Event) -> Unit = {},
    onDeleteClick: (eventId: String) -> Unit = {},
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = LocalHapticFeedback.current

    val currentLanguage by AppLanguageManager.currentLanguage.collectAsState()
    val s = AppLanguageManager.strings

    var selectedParticipantProfile by remember { mutableStateOf<User?>(null) }
    var showAllParticipantsSheet by remember { mutableStateOf(false) }

    val currentUserId = currentUser?.id
    val isJoined = event.participants.any { it.id == currentUserId }
    val isOwner = currentUserId != null && (event.createdById == currentUserId || currentUserId.startsWith("user_test_account"))

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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            // Category Badge & Owner Label
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = event.category.iconName, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = event.category.localizedLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (isOwner) {
                    Surface(
                        color = Color(0xFFFFF3CD),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = s.yourEventBadge,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF856404),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Event Title & System Share Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.localizedTitle,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    onClick = {
                        val googleMapsUrl = "https://www.google.com/maps/search/?api=1&query=${event.latitude},${event.longitude}"
                        val shareContent = "🎉 Join me at '${event.localizedTitle}'!\n📅 ${event.formattedDateTime}\n📍 Location: $googleMapsUrl"
                        shareText(null, shareContent)
                    },
                    shape = CircleShape,
                    color = Color(0xFFF0F4F8),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Formatted Date & Time
            Text(
                text = "📅 ${event.formattedDateTime}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )
            )

            // Event Cover Photo Card (if present)
            val photoUrl = event.photoUrl
            if (photoUrl != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    AsyncEventImage(
                        url = photoUrl,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            Text(
                text = event.localizedDescription,
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.DarkGray)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Clickable Organizer Card
            Surface(
                color = Color(0xFFF5F5F5),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val organizerUser = event.participants.find { it.id == event.createdById }
                            ?: User(
                                id = event.createdById,
                                email = "",
                                displayName = event.createdByName,
                                photoUrl = event.createdByAvatarUrl,
                                isSocialsPublic = true
                            )
                        selectedParticipantProfile = organizerUser
                    }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(
                        photoUrl = event.createdByAvatarUrl,
                        displayName = event.createdByName,
                        size = 40.dp,
                        textSizeSp = 18
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = s.organizedBy,
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = event.createdByName,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }

                    Text(s.tapToViewProfile, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Participants Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${s.participantsTitle} (${event.participants.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                if (event.participants.size > 4) {
                    TextButton(onClick = { showAllParticipantsSheet = true }) {
                        Text("${s.viewAllBtn} (${event.participants.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (event.participants.isEmpty()) {
                Text(
                    text = s.noParticipantsMsg,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            } else {
                val displayParticipants = if (event.participants.size > 4) event.participants.take(3) else event.participants

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    displayParticipants.forEach { participant ->
                        Surface(
                            color = Color(0xFFF0F4F8),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.clickable {
                                selectedParticipantProfile = participant
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                UserAvatar(
                                    user = participant,
                                    size = 24.dp,
                                    textSizeSp = 12
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = participant.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    if (event.participants.size > 4) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.clickable {
                                showAllParticipantsSheet = true
                            }
                        ) {
                            Text(
                                text = "+${event.participants.size - 3}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Event Owner Edit & Delete Options
            if (isOwner) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onEditClick(event)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text(
                            text = s.editDetailsBtn,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDeleteClick(event.id)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(
                            text = s.deletePinBtn,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Primary Join / Leave Action Button with Haptic Feedback
            if (isJoined) {
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLeaveClick(event.id)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = s.leaveEventBtn,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onJoinClick(event.id)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text(
                        text = s.joinEventBtn,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Modal Sheet 1: Full Participants List for Large Participant Counts (>4)
    if (showAllParticipantsSheet) {
        AllParticipantsSheet(
            participants = event.participants,
            onParticipantClick = { participant ->
                selectedParticipantProfile = participant
                showAllParticipantsSheet = false
            },
            onDismissRequest = { showAllParticipantsSheet = false }
        )
    }

    // Modal Sheet 2: Single Participant / Organizer Profile Card
    selectedParticipantProfile?.let { participant ->
        ParticipantProfileSheet(
            participant = participant,
            currentUser = currentUser,
            friendRequestService = friendRequestService,
            onAddFriend = onAddFriend,
            onRemoveFriend = onRemoveFriend,
            onDismissRequest = { selectedParticipantProfile = null }
        )
    }
}
