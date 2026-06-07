package com.vintage4life.routeplanner.distance

import com.vintage4life.routeplanner.model.Location
import org.json.JSONObject
import java.net.URL

/**
 * Result data from the Mapbox Directions API.
 */
data class MapboxRouteResponse(
    val geometry: List<DoubleArray>,
    val distanceMeters: Double,
    val durationSeconds: Double
)

/**
 * Fetches real road geometry and metrics from the Mapbox Directions REST API.
 *
 * Returns an ordered list of [longitude, latitude] coordinate pairs
 * representing the actual road path between the given stops.
 *
 * All methods are blocking — call from a background thread (e.g. Dispatchers.IO).
 */
class MapboxDirectionsClient(private val accessToken: String) {

    /**
     * Fetches road-snapped geometry and metrics for an ordered list of stops.
     *
     * @param stops  At least 2 locations to route through
     * @return       MapboxRouteResponse containing geometry and metrics
     */
    fun fetchRouteData(stops: List<Location>): MapboxRouteResponse? {
        if (stops.size < 2) return null

        val coords = stops.joinToString(";") { "${it.longitude},${it.latitude}" }
        val url = "https://api.mapbox.com/directions/v5/mapbox/driving/$coords" +
                "?geometries=geojson&overview=full&access_token=$accessToken"

        return try {
            val response = URL(url).readText()
            val json    = JSONObject(response)
            val routes  = json.getJSONArray("routes")
            if (routes.length() == 0) return null

            val firstRoute = routes.getJSONObject(0)
            val distance = firstRoute.getDouble("distance")
            val duration = firstRoute.getDouble("duration")

            val coordinates = firstRoute
                .getJSONObject("geometry")
                .getJSONArray("coordinates")

            val geometry = (0 until coordinates.length()).map { i ->
                val pair = coordinates.getJSONArray(i)
                doubleArrayOf(pair.getDouble(0), pair.getDouble(1))
            }

            MapboxRouteResponse(geometry, distance, duration)
        } catch (e: Exception) {
            null
        }
    }
}
