package com.example.projectsigma.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
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

@Composable
expect fun GalleryImagePickerContainer(
    onImagePicked: (String) -> Unit,
    interactionSource: MutableInteractionSource? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
)
