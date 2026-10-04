package com.example.projectsigma.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun AsyncEventImage(
    url: String,
    modifier: Modifier = Modifier
)

@Composable
expect fun GalleryImagePickerButton(
    onImagePicked: (String) -> Unit,
    modifier: Modifier = Modifier
)
