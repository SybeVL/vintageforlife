package com.vintage4life.routeplanner.distance

import com.vintage4life.routeplanner.model.Location
import org.json.JSONObject
import java.net.URL

/**
 * Fetches real road geometry from the Mapbox Directions REST API.
 *
 * Returns an ordered list of [longitude, latitude] coordinate pairs
 * representing the actual road path between the given stops.
 *
 * All methods are blocking — call from a background thread (e.g. Dispatchers.IO).
 */
class MapboxDirectionsClient(private val accessToken: String) {

    /**
     * Fetches road-snapped geometry for an ordered list of stops.
     *
     * @param stops  At least 2 locations to route through
     * @return       List of [lon, lat] pairs along the road; empty on error
     */
    fun fetchRouteGeometry(stops: List<Location>): List<DoubleArray> {
        if (stops.size < 2) return emptyList()

        val coords = stops.joinToString(";") { "${it.longitude},${it.latitude}" }
        val url = "https://api.mapbox.com/directions/v5/mapbox/driving/$coords" +
                "?geometries=geojson&overview=full&access_token=$accessToken"

        return try {
            val response = URL(url).readText()
            val json    = JSONObject(response)
            val routes  = json.getJSONArray("routes")
            if (routes.length() == 0) return emptyList()

            val coordinates = routes
                .getJSONObject(0)
                .getJSONObject("geometry")
                .getJSONArray("coordinates")

            (0 until coordinates.length()).map { i ->
                val pair = coordinates.getJSONArray(i)
                doubleArrayOf(pair.getDouble(0), pair.getDouble(1))
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
