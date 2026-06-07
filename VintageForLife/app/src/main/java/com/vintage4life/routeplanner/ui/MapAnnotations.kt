package com.vintage4life.routeplanner.ui

import com.mapbox.geojson.Feature
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.vintage4life.routeplanner.model.Location

/**
 * Utility object for drawing route lines and stop pins on a Mapbox MapView.
 *
 * Separated from UI composables to keep map drawing logic isolated,
 * reusable and independently testable.
 */
object MapAnnotations {

    private const val SOURCE_ID = "route-source"
    private const val LAYER_ID  = "route-line"

    /**
     * Draws the road route on the map and places numbered text pins at each stop.
     * Uses real road geometry when available; falls back to straight lines otherwise.
     */
    fun drawRoute(
        mapView: MapView,
        stops: List<Location>,
        geometry: List<DoubleArray>,
        annotationManager: PointAnnotationManager
    ) {
        if (stops.size < 2) return

        val points: List<Point> = if (geometry.isNotEmpty()) {
            geometry.map { Point.fromLngLat(it[0], it[1]) }
        } else {
            val pts = stops.map { Point.fromLngLat(it.longitude, it.latitude) }
            pts + pts.first()
        }

        mapView.mapboxMap.getStyle { style ->
            if (style.styleLayerExists(LAYER_ID))  style.removeStyleLayer(LAYER_ID)
            if (style.styleSourceExists(SOURCE_ID)) style.removeStyleSource(SOURCE_ID)

            style.addSource(geoJsonSource(SOURCE_ID) {
                feature(Feature.fromGeometry(LineString.fromLngLats(points)))
            })
            style.addLayer(lineLayer(LAYER_ID, SOURCE_ID) {
                lineColor("#1D9E75")
                lineWidth(3.5)
                lineOpacity(0.85)
            })

            showPins(stops, annotationManager)
        }
    }

    /** Places numbered text pins at each stop location. */
    fun showPins(stops: List<Location>, annotationManager: PointAnnotationManager) {
        annotationManager.deleteAll()
        stops.forEachIndexed { i, loc ->
            annotationManager.create(
                PointAnnotationOptions()
                    .withPoint(Point.fromLngLat(loc.longitude, loc.latitude))
                    .withTextField("${i + 1}. ${loc.name.ifBlank { loc.address }}")
                    .withTextSize(12.0)
                    .withTextColor("#D32F2F")
                    .withTextOffset(listOf(0.0, -1.5))
            )
        }
    }

    /** Removes the route line and all pin annotations from the map. */
    fun clearRoute(mapView: MapView, annotationManager: PointAnnotationManager) {
        mapView.mapboxMap.getStyle { style ->
            if (style.styleLayerExists(LAYER_ID))  style.removeStyleLayer(LAYER_ID)
            if (style.styleSourceExists(SOURCE_ID)) style.removeStyleSource(SOURCE_ID)
        }
        annotationManager.deleteAll()
    }
}
