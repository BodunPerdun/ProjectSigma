package com.example.projectsigma.ui.main

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.projectsigma.model.Event
import com.example.projectsigma.model.EventCluster
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.LocationComponentOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point

private const val PRIMARY_COMMERCIAL_STYLE_URL = "https://tiles.openfreemap.org/styles/positron"
private const val EVENTS_SOURCE_ID = "events-geojson-source"
private const val EVENTS_LAYER_ID = "events-symbol-layer"

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
    var isStyleLoaded by remember { mutableStateOf(false) }

    // Always keep freshest references to state flows & callbacks inside permanent listener
    val currentClusters by rememberUpdatedState(clusters)
    val currentOnMarkerClick by rememberUpdatedState(onMarkerClick)
    val currentOnClusterClick by rememberUpdatedState(onClusterClick)
    val currentOnMapClick by rememberUpdatedState(onMapClick)

    // ActivityResultLauncher for Google Play compliant GPS runtime permissions
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            val map = mapLibreMap
            if (map != null) {
                enableLocationComponent(context, map)
            }
        }
    }

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
                    Log.d("MapLibreMapContainer", "MapLibre style loaded successfully!")

                    // 1. Create and add empty GeoJsonSource
                    val geoJsonSource = GeoJsonSource(EVENTS_SOURCE_ID, FeatureCollection.fromFeatures(emptyArray()))
                    style.addSource(geoJsonSource)

                    // 2. Create and add SymbolLayer
                    val symbolLayer = SymbolLayer(EVENTS_LAYER_ID, EVENTS_SOURCE_ID).apply {
                        setProperties(
                            PropertyFactory.iconImage(Expression.get("icon_image_id")),
                            PropertyFactory.iconAllowOverlap(true),
                            PropertyFactory.iconIgnorePlacement(true),
                            PropertyFactory.iconAnchor(Property.ICON_ANCHOR_CENTER)
                        )
                    }
                    style.addLayer(symbolLayer)

                    isStyleLoaded = true

                    // Try enabling GPS location component if permissions already granted
                    enableLocationComponent(context, map)

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

                // Listen to camera zoom idle changes to dynamically re-cluster / un-cluster and scale pins
                map.addOnCameraIdleListener {
                    val currentZoomInt = map.cameraPosition.zoom.toInt()
                    onZoomChanged(currentZoomInt)
                }

                // Robust single map click listener with queryRenderedFeatures specifically on EVENTS_LAYER_ID
                map.addOnMapClickListener { point ->
                    val screenPoint = map.projection.toScreenLocation(point)
                    val touchRect = RectF(
                        screenPoint.x - 36f,
                        screenPoint.y - 36f,
                        screenPoint.x + 36f,
                        screenPoint.y + 36f
                    )

                    val hitFeatures = map.queryRenderedFeatures(touchRect, EVENTS_LAYER_ID)

                    if (hitFeatures.isNotEmpty()) {
                        val feature = hitFeatures.first()
                        val type = feature.getStringProperty("type") ?: ""
                        val id = feature.getStringProperty("id") ?: ""

                        Log.d("MapLibreMapContainer", "Hit feature '$type' on $EVENTS_LAYER_ID with ID: $id")

                        if (type == "cluster") {
                            val clusterMatch = currentClusters.find { it.id == id }
                            if (clusterMatch != null) {
                                currentOnClusterClick(clusterMatch)
                            }
                        } else if (type == "single") {
                            currentOnMarkerClick(id)
                        }
                        return@addOnMapClickListener true
                    }

                    Log.d("MapLibreMapContainer", "Empty map space tapped at Lat: ${point.latitude}, Lng: ${point.longitude}")
                    currentOnMapClick(point.latitude, point.longitude)
                    true
                }
            }
        }
    }

    // Prompt location permissions on style load
    LaunchedEffect(isStyleLoaded) {
        if (isStyleLoaded) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
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

    // Dynamic GeoJSON source feature updates on Main thread after style is fully loaded
    LaunchedEffect(isStyleLoaded, clusters, selectedEvent) {
        if (!isStyleLoaded) return@LaunchedEffect
        val map = mapLibreMap ?: return@LaunchedEffect

        withContext(Dispatchers.Main) {
            updateGeoJsonFeatures(
                context = context,
                map = map,
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

private fun enableLocationComponent(
    context: Context,
    map: MapLibreMap
) {
    val fineGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val coarseGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    if (fineGranted || coarseGranted) {
        try {
            val style = map.style ?: return
            val locationComponent = map.locationComponent
            val locationComponentOptions = LocationComponentOptions.builder(context)
                .pulseEnabled(true)
                .build()

            val activationOptions = LocationComponentActivationOptions.builder(context, style)
                .locationComponentOptions(locationComponentOptions)
                .build()

            locationComponent.activateLocationComponent(activationOptions)
            locationComponent.isLocationComponentEnabled = true
            locationComponent.cameraMode = CameraMode.TRACKING
            locationComponent.renderMode = RenderMode.COMPASS

            val lastLoc = locationComponent.lastKnownLocation
            if (lastLoc != null) {
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(lastLoc.latitude, lastLoc.longitude),
                        15.5
                    ),
                    1200
                )
            }
        } catch (e: Exception) {
            Log.e("MapLibreMapContainer", "Error enabling location component: ${e.message}")
        }
    }
}

private fun updateGeoJsonFeatures(
    context: Context,
    map: MapLibreMap,
    clusters: List<EventCluster>,
    selectedEvent: Event?
) {
    val style = map.style ?: return
    val source = style.getSourceAs<GeoJsonSource>(EVENTS_SOURCE_ID) ?: return

    val currentZoomLevel = map.cameraPosition.zoom
    // Scale factor: when user zooms in close (zoom 16-18), enlarge pins!
    val iconScale = (currentZoomLevel / 14.0).toFloat().coerceIn(0.7f, 1.8f)

    val features = mutableListOf<Feature>()

    for (cluster in clusters) {
        if (cluster.count == 1) {
            val event = cluster.events.firstOrNull() ?: continue
            val isSelected = event.id == selectedEvent?.id

            val rawBitmap = createRawBitmapIcon(
                context = context,
                text = event.category.iconName,
                hexColor = event.category.colorHex,
                isCluster = false,
                isSelected = isSelected,
                zoomScale = iconScale
            )

            val imageId = "img_evt_${event.id}_${isSelected}_${(iconScale * 10).toInt()}"
            style.addImage(imageId, rawBitmap)

            val feature = Feature.fromGeometry(
                Point.fromLngLat(event.longitude, event.latitude)
            ).apply {
                addStringProperty("id", event.id)
                addStringProperty("type", "single")
                addStringProperty("icon_image_id", imageId)
            }

            features.add(feature)
        } else {
            val isSelected = cluster.events.any { it.id == selectedEvent?.id }

            val rawBitmap = createRawBitmapIcon(
                context = context,
                text = "${cluster.count}",
                hexColor = "#3F51B5",
                isCluster = true,
                isSelected = isSelected,
                zoomScale = iconScale
            )

            val imageId = "img_cls_${cluster.id}_${isSelected}_${(iconScale * 10).toInt()}"
            style.addImage(imageId, rawBitmap)

            val feature = Feature.fromGeometry(
                Point.fromLngLat(cluster.longitude, cluster.latitude)
            ).apply {
                addStringProperty("id", cluster.id)
                addStringProperty("type", "cluster")
                addStringProperty("icon_image_id", imageId)
            }

            features.add(feature)
        }
    }

    source.setGeoJson(FeatureCollection.fromFeatures(features))
}

private fun createRawBitmapIcon(
    context: Context,
    text: String,
    hexColor: String,
    isCluster: Boolean,
    isSelected: Boolean,
    zoomScale: Float = 1.0f
): Bitmap {
    val baseSize = if (isCluster) 110 else 90
    val size = (baseSize * zoomScale).toInt().coerceAtLeast(40)

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
    canvas.drawCircle(size / 2f, size / 2f + (4f * zoomScale), size / 2f - (4f * zoomScale), shadowPaint)

    // Base circle
    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorInt
        style = Paint.Style.FILL
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - (6f * zoomScale), bgPaint)

    // Border
    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = if (isSelected) AndroidColor.parseColor("#FFD700") else AndroidColor.WHITE
        style = Paint.Style.STROKE
        strokeWidth = (if (isSelected) 8f else 5f) * zoomScale
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - (6f * zoomScale), borderPaint)

    // Icon or Count Text
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.WHITE
        textSize = (if (isCluster) 38f else 32f) * zoomScale
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    val yPos = (size / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
    canvas.drawText(text, size / 2f, yPos, textPaint)

    return bitmap
}
