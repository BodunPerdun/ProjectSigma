package com.example.projectsigma.clustering

import com.example.projectsigma.model.Event
import com.example.projectsigma.model.EventCluster
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

object ClusterManager {

    /**
     * Clusters events based on zoom level.
     * Zoom >= 14: Distance threshold is near-zero (~10 meters), so events separate into individual pins.
     * Zoom < 14: Nearby events group into clusters with count badges.
     */
    fun clusterEvents(events: List<Event>, zoomLevel: Int = 15): List<EventCluster> {
        if (events.isEmpty()) return emptyList()

        // If zoom is street-level (>= 14), disable clustering and return individual events
        if (zoomLevel >= 14) {
            return events.map { event ->
                EventCluster(
                    id = "single_${event.id}",
                    latitude = event.latitude,
                    longitude = event.longitude,
                    count = 1,
                    events = listOf(event)
                )
            }
        }

        // Calculate grid cell threshold in degrees based on zoom level
        val thresholdDegrees = calculateThreshold(zoomLevel)

        val processed = mutableSetOf<String>()
        val clusters = mutableListOf<EventCluster>()

        for (event in events) {
            if (processed.contains(event.id)) continue

            val nearbyEvents = mutableListOf<Event>()
            nearbyEvents.add(event)
            processed.add(event.id)

            for (other in events) {
                if (processed.contains(other.id)) continue

                val distance = calculateDistance(
                    event.latitude, event.longitude,
                    other.latitude, other.longitude
                )

                if (distance <= thresholdDegrees) {
                    nearbyEvents.add(other)
                    processed.add(other.id)
                }
            }

            val avgLat = nearbyEvents.map { it.latitude }.average()
            val avgLng = nearbyEvents.map { it.longitude }.average()

            clusters.add(
                EventCluster(
                    id = if (nearbyEvents.size == 1) "single_${event.id}" else "cluster_${event.id}_${nearbyEvents.size}",
                    latitude = avgLat,
                    longitude = avgLng,
                    count = nearbyEvents.size,
                    events = nearbyEvents
                )
            )
        }

        return clusters
    }

    private fun calculateThreshold(zoomLevel: Int): Double {
        return when {
            zoomLevel >= 13 -> 0.002   // ~200 meters
            zoomLevel >= 11 -> 0.010   // ~1 km
            zoomLevel >= 9  -> 0.040   // ~4 km
            zoomLevel >= 7  -> 0.150   // ~15 km
            else -> 0.500
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = abs(lat1 - lat2)
        val dLon = abs(lon1 - lon2)
        return sqrt(dLat.pow(2) + dLon.pow(2))
    }
}
