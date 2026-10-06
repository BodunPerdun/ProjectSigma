package com.example.projectsigma.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectsigma.model.User

@Composable
fun UserAvatar(
    user: User?,
    size: Dp = 40.dp,
    textSizeSp: Int = 16,
    modifier: Modifier = Modifier
) {
    UserAvatar(
        photoUrl = user?.photoUrl,
        displayName = user?.displayName ?: "User",
        size = size,
        textSizeSp = textSizeSp,
        modifier = modifier
    )
}

@Composable
fun UserAvatar(
    photoUrl: String?,
    displayName: String,
    size: Dp = 40.dp,
    textSizeSp: Int = 16,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!photoUrl.isNullOrBlank()) {
            AsyncEventImage(
                url = photoUrl,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = displayName.take(1).uppercase(),
                fontSize = textSizeSp.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
