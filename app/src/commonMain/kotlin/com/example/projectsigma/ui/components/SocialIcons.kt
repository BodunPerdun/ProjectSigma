package com.example.projectsigma.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val InstagramIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Instagram",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color(0xFFE1306C)),
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(12f, 2.163f)
            curveTo(15.204f, 2.163f, 15.584f, 2.175f, 16.85f, 2.233f)
            curveTo(20.102f, 2.381f, 21.621f, 3.924f, 21.769f, 7.152f)
            curveTo(21.827f, 8.417f, 21.838f, 8.797f, 21.838f, 12f)
            curveTo(21.838f, 15.203f, 21.827f, 15.584f, 21.769f, 16.85f)
            curveTo(21.62f, 20.076f, 20.086f, 21.621f, 16.85f, 21.769f)
            curveTo(15.584f, 21.827f, 15.204f, 21.839f, 12f, 21.839f)
            curveTo(8.796f, 21.839f, 8.416f, 21.827f, 7.151f, 21.769f)
            curveTo(3.921f, 21.62f, 2.38f, 20.076f, 2.232f, 16.85f)
            curveTo(2.174f, 15.584f, 2.162f, 15.203f, 2.162f, 12f)
            curveTo(2.162f, 8.797f, 2.174f, 8.417f, 2.232f, 7.151f)
            curveTo(2.381f, 3.924f, 3.92f, 2.38f, 7.151f, 2.233f)
            curveTo(8.416f, 2.175f, 8.796f, 2.163f, 12f, 2.163f)
            close()
            moveTo(12f, 0f)
            curveTo(8.741f, 0f, 8.333f, 0.014f, 7.053f, 0.072f)
            curveTo(2.695f, 0.272f, 0.273f, 2.69f, 0.073f, 7.052f)
            curveTo(0.014f, 8.333f, 0f, 8.741f, 0f, 12f)
            curveTo(0f, 15.259f, 0.014f, 15.668f, 0.072f, 16.948f)
            curveTo(0.272f, 21.306f, 2.69f, 23.728f, 7.052f, 23.928f)
            curveTo(8.333f, 23.986f, 8.741f, 24f, 12f, 24f)
            curveTo(15.259f, 24f, 15.668f, 23.986f, 16.948f, 23.928f)
            curveTo(21.307f, 23.728f, 23.729f, 21.31f, 23.928f, 16.948f)
            curveTo(23.986f, 15.668f, 24f, 15.259f, 24f, 12f)
            curveTo(24f, 8.741f, 23.986f, 8.333f, 23.928f, 7.053f)
            curveTo(23.729f, 2.694f, 21.307f, 0.272f, 16.948f, 0.072f)
            curveTo(15.668f, 0.014f, 15.259f, 0f, 12f, 0f)
            close()
            moveTo(12f, 5.838f)
            curveTo(8.597f, 5.838f, 5.838f, 8.597f, 5.838f, 12f)
            curveTo(5.838f, 15.403f, 8.597f, 18.163f, 12f, 18.163f)
            curveTo(15.403f, 18.163f, 18.163f, 15.403f, 18.163f, 12f)
            curveTo(18.163f, 8.597f, 15.403f, 5.838f, 12f, 5.838f)
            close()
            moveTo(12f, 16f)
            curveTo(9.791f, 16f, 8f, 14.209f, 8f, 12f)
            curveTo(8f, 9.791f, 9.791f, 8f, 12f, 8f)
            curveTo(14.209f, 8f, 16f, 9.791f, 16f, 12f)
            curveTo(16f, 14.209f, 14.209f, 16f, 12f, 16f)
            close()
            moveTo(18.406f, 4.155f)
            curveTo(17.61f, 4.155f, 16.965f, 4.8f, 16.965f, 5.595f)
            curveTo(16.965f, 6.39f, 17.61f, 7.035f, 18.406f, 7.035f)
            curveTo(19.201f, 7.035f, 19.845f, 6.39f, 19.845f, 5.595f)
            curveTo(19.845f, 4.8f, 19.201f, 4.155f, 18.406f, 4.155f)
            close()
        }
    }.build()

val TelegramIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Telegram",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color(0xFF0088CC)),
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(12f, 0f)
            curveTo(5.37f, 0f, 0f, 5.37f, 0f, 12f)
            curveTo(0f, 18.63f, 5.37f, 24f, 12f, 24f)
            curveTo(18.63f, 24f, 24f, 18.63f, 24f, 12f)
            curveTo(24f, 5.37f, 18.63f, 0f, 12f, 0f)
            close()
            moveTo(17.89f, 8.22f)
            lineTo(15.93f, 17.48f)
            curveTo(15.78f, 18.14f, 15.39f, 18.3f, 14.83f, 17.98f)
            lineTo(11.83f, 15.77f)
            lineTo(10.38f, 17.17f)
            curveTo(10.22f, 17.33f, 10.08f, 17.47f, 9.77f, 17.47f)
            lineTo(9.98f, 14.41f)
            lineTo(15.56f, 9.37f)
            curveTo(15.8f, 9.15f, 15.5f, 9.03f, 15.18f, 9.24f)
            lineTo(8.28f, 13.58f)
            lineTo(5.32f, 12.65f)
            curveTo(4.68f, 12.45f, 4.67f, 12.01f, 5.46f, 11.7f)
            lineTo(17.03f, 7.24f)
            curveTo(17.56f, 7.04f, 18.03f, 7.36f, 17.89f, 8.22f)
            close()
        }
    }.build()

@Composable
fun RealInstagramIcon(
    modifier: Modifier = Modifier,
    size: Dp = 16.dp
) {
    Icon(
        imageVector = InstagramIcon,
        contentDescription = "Instagram",
        tint = Color.Unspecified,
        modifier = modifier.size(size)
    )
}

@Composable
fun RealTelegramIcon(
    modifier: Modifier = Modifier,
    size: Dp = 16.dp
) {
    Icon(
        imageVector = TelegramIcon,
        contentDescription = "Telegram",
        tint = Color.Unspecified,
        modifier = modifier.size(size)
    )
}
