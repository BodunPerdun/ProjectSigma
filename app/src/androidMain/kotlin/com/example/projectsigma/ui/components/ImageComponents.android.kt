package com.example.projectsigma.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.projectsigma.data.remote.KtorHttpClient
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

@Composable
actual fun AsyncEventImage(
    url: String,
    modifier: Modifier
) {
    val sanitizedModel: Any = remember(url) {
        val trimmed = url.trim()
        val baseUrl = KtorHttpClient.BASE_URL.removeSuffix("/")

        when {
            // Base64 Data URIs (data:image/jpeg;base64,...)
            trimmed.startsWith("data:image") -> {
                try {
                    val base64Data = trimmed.substringAfter("base64,")
                    Base64.decode(base64Data, Base64.DEFAULT)
                } catch (e: Exception) {
                    trimmed
                }
            }

            // Local file paths on Android device
            trimmed.startsWith("content://") || trimmed.startsWith("file://") -> trimmed
            trimmed.startsWith("/data/") || trimmed.startsWith("/storage/") || File(trimmed).exists() -> File(trimmed)

            // Web URLs from Ktor Server
            trimmed.startsWith("http://localhost:8080") -> trimmed.replace("http://localhost:8080", baseUrl)
            trimmed.startsWith("http://10.0.2.2:8080") -> trimmed.replace("http://10.0.2.2:8080", baseUrl)
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            trimmed.startsWith("/") -> "$baseUrl$trimmed"
            else -> trimmed
        }
    }

    coil.compose.AsyncImage(
        model = sanitizedModel,
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
            val base64Path = saveImageToInternalStorage(context, uri)
            onImagePicked(base64Path)
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
            val base64Path = saveImageToInternalStorage(context, uri)
            onImagePicked(base64Path)
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
        val base64Data = convertImageFileToBase64(permanentFile)
        Log.d("ImagePicker", "Saved permanent image file and generated Base64 data (length ${base64Data.length})")
        base64Data
    } catch (e: Exception) {
        Log.e("ImagePicker", "Error saving image to internal storage: ${e.message}")
        uri.toString()
    }
}

private fun convertImageFileToBase64(file: File): String {
    return try {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return file.absolutePath
        val byteArrayOutputStream = ByteArrayOutputStream()
        val maxDimension = 800
        val scale = Math.min(maxDimension.toFloat() / bitmap.width, maxDimension.toFloat() / bitmap.height).coerceAtMost(1.0f)
        val scaledWidth = (bitmap.width * scale).toInt()
        val scaledHeight = (bitmap.height * scale).toInt()
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)

        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
        "data:image/jpeg;base64,$base64String"
    } catch (e: Exception) {
        Log.e("ImagePicker", "Error encoding base64 image: ${e.message}")
        file.absolutePath
    }
}
