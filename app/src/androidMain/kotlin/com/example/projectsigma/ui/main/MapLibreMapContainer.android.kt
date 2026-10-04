package com.example.projectsigma.ui.main

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.projectsigma.model.Event
import com.example.projectsigma.model.EventCluster
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.Icon
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory

// Commercial-grade vector style: OpenFreeMap Positron (clean, neutral, non-cartoonish Uber-style palette)
private const val PRIMARY_COMMERCIAL_STYLE_URL = "https://tiles.openfreemap.org/styles/positron"

@Composable
actual fun MapLibreMapContainer(
    clusters: List<EventCluster>,
    selectedEvent: Event?,
    onMarkerClick: (eventId: String) -> Unit,
    onClusterClick: (cluster: EventCluster) -> Unit,
    onMapClick: (latitude: Double, longitude: Double) -> Unit,
    modifier: Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    remember {
        MapLibre.getInstance(context)
    }

    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    val markerIdMap = remember { mutableMapOf<Marker, String>() }

    val mapView = remember {
        MapView(context).apply {
            layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )

            addOnDidFailLoadingMapListener { errorMessage ->
                Log.e("MapLibreMapContainer", "Map failed loading: $errorMessage")
            }

            getMapAsync { map ->
                mapLibreMap = map

                // Disable compass widget
                map.uiSettings.isCompassEnabled = false

                map.setStyle(PRIMARY_COMMERCIAL_STYLE_URL) { style ->
                    Log.d("MapLibreMapContainer", "Commercial style loaded successfully!")

                    // Filter out commercial POI clutter while preserving house numbers and street address labels
                    style.layers.forEach { layer ->
                        val layerId = layer.id.lowercase()
                        val isAddressOrBuilding = layerId.contains("house") ||
                                layerId.contains("number") ||
                                layerId.contains("addr") ||
                                layerId.contains("building") ||
                                layerId.contains("street")

                        if (!isAddressOrBuilding && (
                                layerId.contains("poi") ||
                                layerId.contains("store") ||
                                layerId.contains("restaurant") ||
                                layerId.contains("shop") ||
                                layerId.contains("amenity") ||
                                layerId.contains("bank") ||
                                layerId.contains("cafe") ||
                                layerId.contains("bar")
                            )
                        ) {
                            layer.setProperties(PropertyFactory.visibility(Property.NONE))
                        }
                    }
                }

                // Initial camera setup set to street-level zoom (15.0)
                map.cameraPosition = CameraPosition.Builder()
                    .target(LatLng(51.5074, -0.1278)) // City Center
                    .zoom(15.0)                       // Street-level detail
                    .tilt(15.0)                       // Subtle 3D perspective for building footprints
                    .build()

                map.addOnMapClickListener { point ->
                    onMapClick(point.latitude, point.longitude)
                    true
                }
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Dynamic marker re-rendering on data updates
    LaunchedEffect(mapLibreMap, clusters, selectedEvent) {
        val map = mapLibreMap ?: return@LaunchedEffect

        map.clear()
        markerIdMap.clear()

        for (cluster in clusters) {
            if (cluster.count == 1) {
                val event = cluster.events.firstOrNull() ?: continue
                val isSelected = event.id == selectedEvent?.id

                val customIcon = createBitmapIcon(
                    context = context,
                    text = event.category.iconName,
                    hexColor = event.category.colorHex,
                    isCluster = false,
                    isSelected = isSelected
                )

                val marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(event.latitude, event.longitude))
                        .title(event.title)
                        .snippet(event.dateTime)
                        .icon(customIcon)
                )
                markerIdMap[marker] = event.id
            } else {
                val isSelected = cluster.events.any { it.id == selectedEvent?.id }

                val customIcon = createBitmapIcon(
                    context = context,
                    text = "${cluster.count}",
                    hexColor = "#3F51B5",
                    isCluster = true,
                    isSelected = isSelected
                )

                val marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(cluster.latitude, cluster.longitude))
                        .title("${cluster.count} Events Cluster")
                        .snippet("Tap to view events")
                        .icon(customIcon)
                )
                markerIdMap[marker] = cluster.id
            }
        }

        map.setOnMarkerClickListener { clickedMarker ->
            val id = markerIdMap[clickedMarker]
            if (id != null) {
                val clusterMatch = clusters.find { it.id == id }
                if (clusterMatch != null && clusterMatch.count > 1) {
                    onClusterClick(clusterMatch)
                } else {
                    onMarkerClick(id)
                }
            }
            true
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier.fillMaxSize()
    )
}

private fun createBitmapIcon(
    context: Context,
    text: String,
    hexColor: String,
    isCluster: Boolean,
    isSelected: Boolean
): Icon {
    val size = if (isCluster) 110 else 90
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val colorInt = try {
        AndroidColor.parseColor(hexColor)
    } catch (e: Exception) {
        AndroidColor.parseColor("#3F51B5")
    }

    // Shadow
    val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.argb(70, 0, 0, 0)
    }
    canvas.drawCircle(size / 2f, size / 2f + 4f, size / 2f - 4f, shadowPaint)

    // Base circle
    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorInt
        style = Paint.Style.FILL
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 6f, bgPaint)

    // Border
    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = if (isSelected) AndroidColor.parseColor("#FFD700") else AndroidColor.WHITE
        style = Paint.Style.STROKE
        strokeWidth = if (isSelected) 8f else 5f
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 6f, borderPaint)

    // Icon or Count Text
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.WHITE
        textSize = if (isCluster) 38f else 32f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    val yPos = (size / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
    canvas.drawText(text, size / 2f, yPos, textPaint)

    return IconFactory.getInstance(context).fromBitmap(bitmap)
}
