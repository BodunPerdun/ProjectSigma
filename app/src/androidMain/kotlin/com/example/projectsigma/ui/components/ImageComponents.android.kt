package com.example.projectsigma.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
actual fun AsyncEventImage(
    url: String,
    modifier: Modifier
) {
    coil.compose.AsyncImage(
        model = url,
        contentDescription = "Event photo",
        contentScale = ContentScale.Crop,
        modifier = modifier
    )
}

@Composable
actual fun GalleryImagePickerButton(
    onImagePicked: (String) -> Unit,
    modifier: Modifier
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onImagePicked(uri.toString())
        }
    }

    OutlinedButton(
        onClick = {
            launcher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text("🖼️ Pick Photo from Device Gallery", fontWeight = FontWeight.SemiBold)
    }
}
