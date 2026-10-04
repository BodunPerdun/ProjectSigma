package com.example.projectsigma.clustering

import com.example.projectsigma.model.Event
import com.example.projectsigma.model.EventCluster
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

object ClusterManager {

    /**
     * Clusters events based on zoom level.
     * Higher zoom levels (e.g. 15-18) result in smaller cluster radius (individual pins).
     * Lower zoom levels (e.g. 5-10) result in larger cluster radius (grouped pins with counts).
     */
    fun clusterEvents(events: List<Event>, zoomLevel: Int = 13): List<EventCluster> {
        if (events.isEmpty()) return emptyList()

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
            zoomLevel >= 16 -> 0.0005
            zoomLevel >= 14 -> 0.003
            zoomLevel >= 12 -> 0.015
            zoomLevel >= 10 -> 0.05
            zoomLevel >= 8  -> 0.2
            else -> 0.8
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = abs(lat1 - lat2)
        val dLon = abs(lon1 - lon2)
        return sqrt(dLat.pow(2) + dLon.pow(2))
    }
}
