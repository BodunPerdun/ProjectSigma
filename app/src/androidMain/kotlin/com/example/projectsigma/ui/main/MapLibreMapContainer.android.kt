package com.example.projectsigma.ui.main

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import com.example.projectsigma.theme.AppThemeManager
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point

private const val EVENTS_SOURCE_ID = "events-geojson-source"
private const val EVENTS_LAYER_ID = "events-symbol-layer"

@Composable
actual fun MapLibreMapContainer(
    clusters: List<EventCluster>,
    selectedEvent: Event?,
    newPinLocation: Pair<Double, Double>?,
    onMarkerClick: (eventId: String) -> Unit,
    onClusterClick: (cluster: EventCluster) -> Unit,
    onMapClick: (latitude: Double, longitude: Double) -> Unit,
    onZoomChanged: (zoom: Int) -> Unit,
    myLocationTrigger: Int,
    modifier: Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    remember {
        MapLibre.getInstance(context)
    }

    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var isStyleLoaded by remember { mutableStateOf(false) }

    val activeStyleUrl = AppThemeManager.mapStyleUrl

    val currentClusters by rememberUpdatedState(clusters)
    val currentOnMarkerClick by rememberUpdatedState(onMarkerClick)
    val currentOnClusterClick by rememberUpdatedState(onClusterClick)
    val currentOnMapClick by rememberUpdatedState(onMapClick)

    // Physics-based spring bounce animation for new draft pin entrance
    val draftPinScale = remember { Animatable(0f) }

    LaunchedEffect(newPinLocation) {
        if (newPinLocation != null) {
            draftPinScale.snapTo(0.2f)
            draftPinScale.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        } else {
            draftPinScale.snapTo(0f)
        }
    }

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
            getMapAsync { map ->
                mapLibreMap = map

                // Initial camera target: London (51.5074, -0.1278)
                map.cameraPosition = CameraPosition.Builder()
                    .target(LatLng(51.5074, -0.1278))
                    .zoom(12.5)
                    .build()

                map.setStyle(activeStyleUrl) { style ->
                    Log.d("MapLibre", "Map style loaded successfully: $activeStyleUrl")
                    isStyleLoaded = true
                    initGeoJsonLayers(style)
                    enableLocationComponent(context, map)

                    map.addOnCameraMoveListener {
                        val zoomInt = map.cameraPosition.zoom.toInt()
                        onZoomChanged(zoomInt)
                    }

                    map.addOnMapClickListener { latLng ->
                        val screenPoint = map.projection.toScreenLocation(latLng)
                        val features = map.queryRenderedFeatures(screenPoint, EVENTS_LAYER_ID)

                        if (features.isNotEmpty()) {
                            val clickedFeature = features.first()
                            val eventId = clickedFeature.getStringProperty("id") ?: ""
                            val featureType = clickedFeature.getStringProperty("type") ?: ""

                            if (eventId.isNotBlank()) {
                                if (featureType == "cluster") {
                                    val matchedCluster = currentClusters.find { it.id == eventId }
                                    if (matchedCluster != null) {
                                        currentOnClusterClick(matchedCluster)
                                    }
                                } else {
                                    currentOnMarkerClick(eventId)
                                }
                                return@addOnMapClickListener true
                            }
                        }

                        currentOnMapClick(latLng.latitude, latLng.longitude)
                        true
                    }
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

    LaunchedEffect(myLocationTrigger) {
        if (myLocationTrigger > 0) {
            val map = mapLibreMap
            if (map != null) {
                val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

                if (hasFine || hasCoarse) {
                    val locationComp = map.locationComponent
                    if (locationComp.isLocationComponentActivated && locationComp.lastKnownLocation != null) {
                        val loc = locationComp.lastKnownLocation!!
                        map.animateCamera(
                            CameraUpdateFactory.newCameraPosition(
                                CameraPosition.Builder()
                                    .target(LatLng(loc.latitude, loc.longitude))
                                    .zoom(15.0)
                                    .build()
                            ),
                            1200
                        )
                    }
                } else {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }
        }
    }

    LaunchedEffect(selectedEvent) {
        val map = mapLibreMap
        if (map != null && selectedEvent != null) {
            map.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder()
                        .target(LatLng(selectedEvent.latitude, selectedEvent.longitude))
                        .zoom(15.0)
                        .build()
                ),
                1000
            )
        }
    }

    LaunchedEffect(clusters, selectedEvent, newPinLocation, draftPinScale.value, isStyleLoaded) {
        val map = mapLibreMap
        if (map != null && isStyleLoaded) {
            val style = map.style
            if (style != null && style.isFullyLoaded) {
                val zoomScale = calculateZoomScale(map.cameraPosition.zoom)
                updateMapLayers(
                    context = context,
                    style = style,
                    clusters = clusters,
                    selectedEvent = selectedEvent,
                    newPinLocation = newPinLocation,
                    draftPinScale = draftPinScale.value,
                    zoomScale = zoomScale
                )
            }
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
    )
}

private fun calculateZoomScale(zoomLevel: Double): Float {
    return when {
        zoomLevel >= 15.0 -> 1.25f
        zoomLevel >= 13.0 -> 1.0f
        zoomLevel >= 10.0 -> 0.85f
        else -> 0.7f
    }
}

private fun initGeoJsonLayers(style: Style) {
    if (style.getSource(EVENTS_SOURCE_ID) == null) {
        val geoJsonSource = GeoJsonSource(EVENTS_SOURCE_ID)
        style.addSource(geoJsonSource)

        val symbolLayer = SymbolLayer(EVENTS_LAYER_ID, EVENTS_SOURCE_ID).apply {
            withProperties(
                PropertyFactory.iconImage(Expression.get("icon_image_id")),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.iconIgnorePlacement(true)
            )
        }
        style.addLayer(symbolLayer)
    }
}

private fun enableLocationComponent(context: Context, map: MapLibreMap) {
    val style = map.style ?: return
    val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    if (hasFine || hasCoarse) {
        try {
            val locationComponent = map.locationComponent
            val options = LocationComponentActivationOptions.builder(context, style).build()
            locationComponent.activateLocationComponent(options)
            locationComponent.isLocationComponentEnabled = true
            locationComponent.cameraMode = CameraMode.NONE
            locationComponent.renderMode = RenderMode.COMPASS

            // Instantly animate camera to user GPS position upon launch
            val lastLoc = locationComponent.lastKnownLocation
            if (lastLoc != null) {
                map.animateCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder()
                            .target(LatLng(lastLoc.latitude, lastLoc.longitude))
                            .zoom(14.5)
                            .build()
                    ),
                    1000
                )
            }
        } catch (e: Exception) {
            println("[MapLibre] Error activating location component: ${e.message}")
        }
    }
}

private fun updateMapLayers(
    context: Context,
    style: Style,
    clusters: List<EventCluster>,
    selectedEvent: Event?,
    newPinLocation: Pair<Double, Double>?,
    draftPinScale: Float,
    zoomScale: Float
) {
    val source = style.getSourceAs<GeoJsonSource>(EVENTS_SOURCE_ID) ?: return
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
                zoomScale = zoomScale
            )

            val imageId = "img_evt_${event.id}_${isSelected}_${(zoomScale * 10).toInt()}"
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
                zoomScale = zoomScale
            )

            val imageId = "img_cls_${cluster.id}_${isSelected}_${(zoomScale * 10).toInt()}"
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

    // Physical Draft Pin Rendering on Map Canvas with Spring Bounce Scale Animation
    if (newPinLocation != null) {
        val animatedZoomScale = zoomScale * 1.35f * draftPinScale.coerceAtLeast(0.1f)

        val draftBitmap = createRawBitmapIcon(
            context = context,
            text = "📍",
            hexColor = "#FF3D00",
            isCluster = false,
            isSelected = true,
            zoomScale = animatedZoomScale
        )

        val draftImageId = "img_draft_pin_${(animatedZoomScale * 100).toInt()}"
        style.addImage(draftImageId, draftBitmap)

        val draftFeature = Feature.fromGeometry(
            Point.fromLngLat(newPinLocation.second, newPinLocation.first)
        ).apply {
            addStringProperty("id", "draft_new_event_pin")
            addStringProperty("type", "draft")
            addStringProperty("icon_image_id", draftImageId)
        }

        features.add(draftFeature)
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

    val parsedColor = try {
        Color.parseColor(hexColor)
    } catch (e: Exception) {
        Color.parseColor("#1976D2")
    }

    val paintCircle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = parsedColor
        style = Paint.Style.FILL
    }

    val paintBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = if (isSelected) Color.YELLOW else Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = if (isSelected) 10f else 6f
    }

    val centerX = size / 2f
    val centerY = size / 2f
    val radius = (size / 2f) - 10f

    if (isSelected) {
        val paintGlow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(100, 255, 235, 59)
            style = Paint.Style.FILL
        }
        canvas.drawCircle(centerX, centerY, radius + 8f, paintGlow)
    }

    canvas.drawCircle(centerX, centerY, radius, paintCircle)
    canvas.drawCircle(centerX, centerY, radius, paintBorder)

    val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = (size * 0.45f)
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    val textBounds = Rect()
    paintText.getTextBounds(text, 0, text.length, textBounds)
    val textY = centerY + (textBounds.height() / 2f) - textBounds.bottom

    canvas.drawText(text, centerX, textY, paintText)

    return bitmap
}
