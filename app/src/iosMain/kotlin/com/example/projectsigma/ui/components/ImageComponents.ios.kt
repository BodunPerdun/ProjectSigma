package com.example.projectsigma.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
actual fun AsyncEventImage(
    url: String,
    modifier: Modifier
) {
    Box(
        modifier = modifier.background(Color(0xFF2C2C2C)),
        contentAlignment = Alignment.Center
    ) {
        Text("📷 Event Photo", color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
actual fun GalleryImagePickerButton(
    onImagePicked: (String) -> Unit,
    modifier: Modifier
) {
    OutlinedButton(
        onClick = {
            onImagePicked("https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600")
        },
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text("🖼️ Select Cover Photo", fontWeight = FontWeight.SemiBold)
    }
}
