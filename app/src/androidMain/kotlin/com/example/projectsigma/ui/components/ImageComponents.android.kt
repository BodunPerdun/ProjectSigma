package com.example.projectsigma.ui.components

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.io.File
import java.io.FileOutputStream

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
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val permanentPath = saveImageToInternalStorage(context, uri)
            onImagePicked(permanentPath)
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

@Composable
actual fun GalleryImagePickerContainer(
    onImagePicked: (String) -> Unit,
    interactionSource: MutableInteractionSource?,
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val permanentPath = saveImageToInternalStorage(context, uri)
            onImagePicked(permanentPath)
        }
    }

    Box(
        modifier = modifier.clickable(
            interactionSource = interactionSource,
            indication = null
        ) {
            launcher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    ) {
        content()
    }
}

private fun saveImageToInternalStorage(context: Context, uri: Uri): String {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return uri.toString()
        val imagesDir = File(context.filesDir, "saved_images")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }
        val permanentFile = File(imagesDir, "img_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(permanentFile)
        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        Log.d("ImagePicker", "Saved permanent image file at: ${permanentFile.absolutePath}")
        permanentFile.absolutePath
    } catch (e: Exception) {
        Log.e("ImagePicker", "Error saving image to internal storage: ${e.message}")
        uri.toString()
    }
}
