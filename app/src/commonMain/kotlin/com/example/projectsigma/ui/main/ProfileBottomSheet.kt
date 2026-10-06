package com.example.projectsigma.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectsigma.i18n.AppLanguageManager
import com.example.projectsigma.model.Event
import com.example.projectsigma.model.User
import com.example.projectsigma.ui.components.AsyncEventImage
import com.example.projectsigma.ui.components.GalleryImagePickerContainer
import com.example.projectsigma.ui.components.UserAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileBottomSheet(
    user: User,
    userEvents: List<Event>,
    onEventClick: (eventId: String) -> Unit = {},
    onFriendsClick: () -> Unit = {},
    onLocationVisibilityChanged: (Boolean) -> Unit = {},
    onSocialsPublicityChanged: (Boolean) -> Unit = {},
    onAvatarPhotoPicked: (String?) -> Unit = {},
    onSocialHandlesUpdated: (instagram: String?, telegram: String?) -> Unit = { _, _ -> },
    onBioUpdated: (String?) -> Unit = {},
    onDismissRequest: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val uriHandler = LocalUriHandler.current

    val currentLanguage by AppLanguageManager.currentLanguage.collectAsState()
    val s = AppLanguageManager.strings

    val avatarInteractionSource = remember { MutableInteractionSource() }
    val isAvatarPressed by avatarInteractionSource.collectIsPressedAsState()

    var showSocialEditDialog by remember { mutableStateOf(false) }
    var showBioEditDialog by remember { mutableStateOf(false) }

    var instaInput by remember { mutableStateOf(user.instagramHandle ?: "") }
    var tgInput by remember { mutableStateOf(user.telegramHandle ?: "") }
    var bioInput by remember { mutableStateOf(user.bio ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Section 1: User Header Block (Avatar, Name, Email Badge)
            Box(contentAlignment = Alignment.TopEnd) {
                GalleryImagePickerContainer(
                    interactionSource = avatarInteractionSource,
                    onImagePicked = { pickedUri -> onAvatarPhotoPicked(pickedUri) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        UserAvatar(
                            user = user,
                            size = 96.dp,
                            textSizeSp = 38
                        )

                        if (isAvatarPressed) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.45f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Change",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                if (user.photoUrl != null) {
                    Surface(
                        onClick = { onAvatarPhotoPicked(null) },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.error,
                        shadowElevation = 6.dp,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "✕",
                                fontSize = 14.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = user.displayName,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Surface(
                color = Color(0xFFF0F4F8),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = user.email,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 2: User Stats Card (Created Events & Clickable Friends Count)
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${userEvents.size}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = s.createdEventsTitle,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    // Clickable Friends Count Column -> Opens Friends Sheet!
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                onFriendsClick()
                                onDismissRequest()
                            }
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${user.friends.size}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = s.friendsCountTitle,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Section 3: Bio Description Card
            Surface(
                color = Color(0xFFF8F9FA),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = s.bioTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        TextButton(onClick = { showBioEditDialog = true }) {
                            Text(s.editBioBtn, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        text = if (!user.bio.isNullOrBlank()) user.bio else s.noBioMsg,
                        fontSize = 12.sp,
                        color = if (!user.bio.isNullOrBlank()) Color.DarkGray else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Section 4: Social Media Deep Link Bar
            Surface(
                color = Color(0xFFF8F9FA),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!user.instagramHandle.isNullOrBlank()) {
                            Surface(
                                color = Color(0xFFFCE4EC),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.clickable {
                                    uriHandler.openUri("https://instagram.com/_u/${user.instagramHandle}")
                                }
                            ) {
                                Text(
                                    text = "📸 @${user.instagramHandle}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC2185B),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }

                        if (!user.telegramHandle.isNullOrBlank()) {
                            Surface(
                                color = Color(0xFFE3F2FD),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.clickable {
                                    uriHandler.openUri("https://t.me/${user.telegramHandle}")
                                }
                            ) {
                                Text(
                                    text = "✈️ @${user.telegramHandle}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1976D2),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }

                        if (user.instagramHandle.isNullOrBlank() && user.telegramHandle.isNullOrBlank()) {
                            Text(s.noSocialsMsg, fontSize = 12.sp, color = Color.Gray)
                        }
                    }

                    TextButton(onClick = { showSocialEditDialog = true }) {
                        Text(s.editSocialsBtn, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Section 5: Grouped Privacy & Settings Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "⚙️ Privacy Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Switch 1: Location Discovery
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = s.visibleNearbyTitle,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = s.visibleNearbySubtitle,
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = user.isLocationVisible,
                            onCheckedChange = { isChecked ->
                                onLocationVisibilityChanged(isChecked)
                            }
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = Color(0xFFE0E0E0)
                    )

                    // Switch 2: Public Social Links
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = s.publicSocialsTitle,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = s.publicSocialsSubtitle,
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = user.isSocialsPublic,
                            onCheckedChange = { isChecked ->
                                onSocialsPublicityChanged(isChecked)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 6: User Created Events List
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = s.createdEventsTitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (userEvents.isEmpty()) {
                Text(
                    text = "You haven't created any events yet. Tap empty map space to add your first event!",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(userEvents) { event ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onEventClick(event.id)
                                    onDismissRequest()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (event.photoUrl != null) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                    ) {
                                        AsyncEventImage(
                                            url = event.photoUrl,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                } else {
                                    Text(
                                        text = event.category.iconName,
                                        fontSize = 24.sp,
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = event.title,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = event.dateTime,
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 7: Sign Out Button
            OutlinedButton(
                onClick = {
                    onLogoutClick()
                    onDismissRequest()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = s.signOutBtn,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Modal Sheet 1: Edit Social Handles (Instagram & Telegram)
    if (showSocialEditDialog) {
        val editSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showSocialEditDialog = false },
            sheetState = editSheetState,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = s.linkSocialsTitle,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = instaInput,
                    onValueChange = { instaInput = it },
                    label = { Text("📸 Instagram Username") },
                    placeholder = { Text("e.g. alex_smith") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = tgInput,
                    onValueChange = { tgInput = it },
                    label = { Text("✈️ Telegram Username") },
                    placeholder = { Text("e.g. alex_smith") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        onSocialHandlesUpdated(instaInput, tgInput)
                        showSocialEditDialog = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(s.done, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Modal Sheet 2: Edit Bio Description
    if (showBioEditDialog) {
        val bioSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showBioEditDialog = false },
            sheetState = bioSheetState,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = s.editBioBtn,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = bioInput,
                    onValueChange = { bioInput = it },
                    label = { Text(s.bioTitle) },
                    placeholder = { Text("Tell other users about your hobbies and interests...") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        onBioUpdated(bioInput)
                        showBioEditDialog = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(s.saveBioBtn, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
