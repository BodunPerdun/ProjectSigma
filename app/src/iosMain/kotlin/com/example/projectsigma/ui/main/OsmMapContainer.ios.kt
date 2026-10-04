package com.example.projectsigma.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import com.example.projectsigma.model.Event
import com.example.projectsigma.model.EventCluster
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL
import platform.WebKit.WKNavigationAction
import platform.WebKit.WKNavigationActionPolicy
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun OsmMapContainer(
    clusters: List<EventCluster>,
    selectedEvent: Event?,
    onMarkerClick: (eventId: String) -> Unit,
    onClusterClick: (cluster: EventCluster) -> Unit,
    onMapClick: (latitude: Double, longitude: Double) -> Unit,
    modifier: Modifier
) {
    val navDelegate = remember(onMarkerClick, onMapClick) {
        object : NSObject(), WKNavigationDelegateProtocol {
            override fun webView(
                webView: WKWebView,
                decidePolicyForNavigationAction: WKNavigationAction,
                decisionHandler: (WKNavigationActionPolicy) -> Unit
            ) {
                val url = decidePolicyForNavigationAction.request.URL?.absoluteString ?: ""
                when {
                    url.startsWith("bridge://marker/") -> {
                        val eventId = url.substringAfter("bridge://marker/")
                        onMarkerClick(eventId)
                        decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyCancel)
                    }
                    url.startsWith("bridge://map/") -> {
                        val coords = url.substringAfter("bridge://map/").split(",")
                        if (coords.size == 2) {
                            val lat = coords[0].toDoubleOrNull() ?: 0.0
                            val lng = coords[1].toDoubleOrNull() ?: 0.0
                            onMapClick(lat, lng)
                        }
                        decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyCancel)
                    }
                    else -> {
                        decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow)
                    }
                }
            }
        }
    }

    val webView = remember {
        val config = WKWebViewConfiguration()
        WKWebView(frame = platform.CoreGraphics.CGRectZero.readValue(), configuration = config).apply {
            navigationDelegate = navDelegate
            loadHTMLString(generateOsmIosHtml(), baseURL = NSURL(string = "https://openstreetmap.org"))
        }
    }

    LaunchedEffect(clusters, selectedEvent) {
        val markersJson = generateIosMarkersJson(clusters, selectedEvent)
        webView.evaluateJavaScript("updateMarkers($markersJson);", completionHandler = null)
    }

    UIKitView(
        factory = { webView },
        modifier = modifier
    )
}

private fun generateIosMarkersJson(clusters: List<EventCluster>, selectedEvent: Event?): String {
    val items = mutableListOf<String>()

    for (cluster in clusters) {
        if (cluster.count == 1) {
            val event = cluster.events.firstOrNull() ?: continue
            val isSelected = event.id == selectedEvent?.id
            val colorHex = event.category.colorHex
            val icon = event.category.iconName
            val title = event.title.replace("'", "\\'")

            items.add(
                """{
                    "id": "${event.id}",
                    "lat": ${event.latitude},
                    "lng": ${event.longitude},
                    "isCluster": false,
                    "count": 1,
                    "color": "$colorHex",
                    "icon": "$icon",
                    "title": "$title",
                    "isSelected": $isSelected
                }"""
            )
        } else {
            val isSelected = cluster.events.any { it.id == selectedEvent?.id }

            items.add(
                """{
                    "id": "${cluster.id}",
                    "lat": ${cluster.latitude},
                    "lng": ${cluster.longitude},
                    "isCluster": true,
                    "count": ${cluster.count},
                    "color": "#3F51B5",
                    "icon": "${cluster.count}",
                    "title": "${cluster.count} Events Cluster",
                    "isSelected": $isSelected
                }"""
            )
        }
    }

    return "[${items.joinToString(",")}]"
}

private fun generateOsmIosHtml(): String {
    return """
    <!DOCTYPE html>
    <html>
    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
        <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
        <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
        <style>
            body, html { margin: 0; padding: 0; height: 100%; width: 100%; }
            #map { height: 100%; width: 100%; background: #e0e0e0; }
            .custom-pin {
                display: flex;
                align-items: center;
                justify-content: center;
                border-radius: 50%;
                color: white;
                font-weight: bold;
                font-family: -apple-system, sans-serif;
                box-shadow: 0 3px 8px rgba(0,0,0,0.4);
                border: 2px solid white;
            }
            .cluster-pin { background: linear-gradient(135deg, #3F51B5, #2196F3); font-size: 14px; }
            .selected-pin { transform: scale(1.3); border: 3px solid #FFD700; z-index: 1000 !important; }
        </style>
    </head>
    <body>
        <div id="map"></div>
        <script>
            var map = L.map('map', { zoomControl: false }).setView([51.5074, -0.1278], 13);
            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 19 }).addTo(map);

            var currentMarkers = [];

            map.on('click', function(e) {
                window.location.href = 'bridge://map/' + e.latlng.lat + ',' + e.latlng.lng;
            });

            function updateMarkers(data) {
                currentMarkers.forEach(function(m) { map.removeLayer(m); });
                currentMarkers = [];

                data.forEach(function(item) {
                    var size = item.isCluster ? 42 : 36;
                    var cssClass = item.isCluster ? 'custom-pin cluster-pin' : 'custom-pin';
                    if (item.isSelected) cssClass += ' selected-pin';

                    var html = item.isCluster ? item.count : item.icon;
                    var icon = L.divIcon({
                        className: cssClass,
                        html: '<div style="background-color:' + item.color + '; width:100%; height:100%; border-radius:50%; display:flex; align-items:center; justify-content:center;">' + html + '</div>',
                        iconSize: [size, size],
                        iconAnchor: [size/2, size/2]
                    });

                    var marker = L.marker([item.lat, item.lng], { icon: icon }).addTo(map);
                    marker.on('click', function(e) {
                        L.DomEvent.stopPropagation(e);
                        window.location.href = 'bridge://marker/' + item.id;
                    });
                    currentMarkers.push(marker);
                });
            }
        </script>
    </body>
    </html>
    """.trimIndent()
}
