package com.example.projectsigma.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.projectsigma.model.Event
import com.example.projectsigma.model.EventCluster

@Composable
actual fun MapLibreMapContainer(
    clusters: List<EventCluster>,
    selectedEvent: Event?,
    onMarkerClick: (eventId: String) -> Unit,
    onClusterClick: (cluster: EventCluster) -> Unit,
    onMapClick: (latitude: Double, longitude: Double) -> Unit,
    onZoomChanged: (zoom: Int) -> Unit,
    myLocationTrigger: Int,
    modifier: Modifier
) {
    Box(
        modifier = modifier.background(Color(0xFFE0E0E0)),
        contentAlignment = Alignment.Center
    ) {
        Text("🗺️ MapLibre Map View")
    }
}
