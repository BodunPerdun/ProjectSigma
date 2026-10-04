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
import com.google.gson.JsonPrimitive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.annotation.SymbolOptions
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory

private const val PRIMARY_COMMERCIAL_STYLE_URL = "https://tiles.openfreemap.org/styles/positron"

@Composable
actual fun MapLibreMapContainer(
    clusters: List<EventCluster>,
    selectedEvent: Event?,
    onMarkerClick: (eventId: String) -> Unit,
    onClusterClick: (cluster: EventCluster) -> Unit,
    onMapClick: (latitude: Double, longitude: Double) -> Unit,
    onZoomChanged: (zoom: Int) -> Unit,
    modifier: Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    remember {
        MapLibre.getInstance(context)
    }

    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var symbolManager by remember { mutableStateOf<SymbolManager?>(null) }
    var isStyleLoaded by remember { mutableStateOf(false) }

    // Timestamp tracker to prevent map click from opening "Create Event" when tapping markers
    var lastSymbolTapTime by remember { mutableStateOf(0L) }

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
                    Log.d("MapLibreMapContainer", "Style loaded successfully!")

                    // Initialize SymbolManager ONLY AFTER style is fully loaded
                    val manager = SymbolManager(this, map, style)
                    manager.iconAllowOverlap = true
                    manager.iconIgnorePlacement = true

                    // Register Click Listener ONCE on SymbolManager using native JsonPrimitive symbol data
                    manager.addClickListener { clickedSymbol ->
                        lastSymbolTapTime = System.currentTimeMillis()
                        val rawDataId = clickedSymbol.data?.asString
                        Log.d("MapLibreMapContainer", "Symbol clicked with data ID: $rawDataId")

                        if (rawDataId != null) {
                            if (rawDataId.startsWith("cluster_")) {
                                val clusterId = rawDataId.substringAfter("cluster_")
                                val clusterMatch = clusters.find { it.id == clusterId }
                                if (clusterMatch != null) {
                                    onClusterClick(clusterMatch)
                                }
                            } else if (rawDataId.startsWith("single_")) {
                                val eventId = rawDataId.substringAfter("single_")
                                onMarkerClick(eventId)
                            }
                        }
                        true
                    }

                    symbolManager = manager
                    isStyleLoaded = true

                    // Filter out commercial POI clutter while preserving house numbers & street address labels
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

                // Initial camera setup
                map.cameraPosition = CameraPosition.Builder()
                    .target(LatLng(51.5074, -0.1278))
                    .zoom(15.0)
                    .tilt(15.0)
                    .build()

                // Listen to camera zoom idle changes to dynamically re-cluster / un-cluster
                map.addOnCameraIdleListener {
                    val currentZoomInt = map.cameraPosition.zoom.toInt()
                    onZoomChanged(currentZoomInt)
                }

                map.addOnMapClickListener { point ->
                    val now = System.currentTimeMillis()
                    // Guard: Ignore map click if a symbol marker was tapped within the last 350ms
                    if (now - lastSymbolTapTime < 350) {
                        Log.d("MapLibreMapContainer", "Map click ignored because symbol marker was tapped recently")
                        return@addOnMapClickListener false
                    }

                    Log.d("MapLibreMapContainer", "Empty map space tapped at Lat: ${point.latitude}, Lng: ${point.longitude}")
                    onMapClick(point.latitude, point.longitude)
                    false
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

    // Dynamic Symbol rendering strictly on Main thread after style is fully loaded
    LaunchedEffect(isStyleLoaded, symbolManager, clusters, selectedEvent) {
        if (!isStyleLoaded) return@LaunchedEffect
        val manager = symbolManager ?: return@LaunchedEffect
        val map = mapLibreMap ?: return@LaunchedEffect

        withContext(Dispatchers.Main) {
            updateSymbolAnnotations(
                context = context,
                map = map,
                manager = manager,
                clusters = clusters,
                selectedEvent = selectedEvent
            )

            // Smooth Camera Fly-To on event selection / creation
            selectedEvent?.let { event ->
                Log.d("MapLibreMapContainer", "Camera easing to created/selected event at Lat: ${event.latitude}, Lng: ${event.longitude}")
                map.easeCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder()
                            .target(LatLng(event.latitude, event.longitude))
                            .zoom(15.5)
                            .build()
                    ),
                    1000
                )
            }
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier.fillMaxSize()
    )
}

private fun updateSymbolAnnotations(
    context: Context,
    map: MapLibreMap,
    manager: SymbolManager,
    clusters: List<EventCluster>,
    selectedEvent: Event?
) {
    val style = map.style ?: return

    manager.deleteAll()

    val currentZoomLevel = map.cameraPosition.zoom
    val iconScale = (currentZoomLevel / 15.0).toFloat().coerceIn(0.7f, 1.4f)
    val optionsList = mutableListOf<SymbolOptions>()

    for (cluster in clusters) {
        if (cluster.count == 1) {
            val event = cluster.events.firstOrNull() ?: continue
            val isSelected = event.id == selectedEvent?.id

            val rawBitmap = createRawBitmapIcon(
                context = context,
                text = event.category.iconName,
                hexColor = event.category.colorHex,
                isCluster = false,
                isSelected = isSelected
            )

            val imageId = "img_evt_${event.id}_${isSelected}"
            style.addImage(imageId, rawBitmap)

            val options = SymbolOptions()
                .withLatLng(LatLng(event.latitude, event.longitude))
                .withIconImage(imageId)
                .withIconSize(iconScale)
                .withIconAnchor(Property.ICON_ANCHOR_CENTER)
                .withData(JsonPrimitive("single_${event.id}"))

            optionsList.add(options)
        } else {
            val isSelected = cluster.events.any { it.id == selectedEvent?.id }

            val rawBitmap = createRawBitmapIcon(
                context = context,
                text = "${cluster.count}",
                hexColor = "#3F51B5",
                isCluster = true,
                isSelected = isSelected
            )

            val imageId = "img_cls_${cluster.id}_${isSelected}"
            style.addImage(imageId, rawBitmap)

            val options = SymbolOptions()
                .withLatLng(LatLng(cluster.latitude, cluster.longitude))
                .withIconImage(imageId)
                .withIconSize(iconScale)
                .withIconAnchor(Property.ICON_ANCHOR_CENTER)
                .withData(JsonPrimitive("cluster_${cluster.id}"))

            optionsList.add(options)
        }
    }

    // Batch create symbol annotations on MapLibre GPU layer
    manager.create(optionsList)
}

private fun createRawBitmapIcon(
    context: Context,
    text: String,
    hexColor: String,
    isCluster: Boolean,
    isSelected: Boolean
): Bitmap {
    val baseSize = if (isCluster) 110 else 90
    val size = baseSize

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

    return bitmap
}
