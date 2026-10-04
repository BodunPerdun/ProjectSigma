package com.example.projectsigma.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.projectsigma.model.Event
import com.example.projectsigma.model.EventCluster

@Composable
expect fun MapLibreMapContainer(
    clusters: List<EventCluster>,
    selectedEvent: Event?,
    onMarkerClick: (eventId: String) -> Unit,
    onClusterClick: (cluster: EventCluster) -> Unit,
    onMapClick: (latitude: Double, longitude: Double) -> Unit,
    onZoomChanged: (zoom: Int) -> Unit,
    modifier: Modifier
)
