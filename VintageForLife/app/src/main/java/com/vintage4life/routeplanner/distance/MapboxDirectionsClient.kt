package com.vintage4life.routeplanner.distance

import com.vintage4life.routeplanner.model.Location
import org.json.JSONObject
import java.net.URL

// uses mapbox api for routing
data class MapboxRouteResponse(
    val geometry: List<DoubleArray>,
    val distanceMeters: Double,
    val durationSeconds: Double
)

// uses real road data to calculate routes
class MapboxDirectionsClient(private val accessToken: String) {
    fun fetchRouteData(stops: List<Location>): MapboxRouteResponse? {
        if (stops.size < 2) return null
        val coords = stops.joinToString(";") { "${it.longitude},${it.latitude}" }
        val url = "https://api.mapbox.com/directions/v5/mapbox/driving/$coords" +
                  "?geometries=geojson&overview=full&access_token=$accessToken"
        return parseDirectionsResponse(url)
    }
    fun buildRoadMatrix(locations: List<Location>): RoadMatrix? {
        val n = locations.size
        val distances = Array(n) { DoubleArray(n) }
        val durations = Array(n) { DoubleArray(n) }

        for (i in 0 until n) {
            for (j in i + 1 until n) {
                val data = fetchPair(locations[i], locations[j]) ?: return null
                // Symmetriseer: rij i→j ≈ j→i voor rijden
                distances[i][j] = data.distanceMeters / 1000.0
                distances[j][i] = data.distanceMeters / 1000.0
                durations[i][j] = data.durationSeconds
                durations[j][i] = data.durationSeconds
            }
        }
        return RoadMatrix(durations, distances)
    }

    // calculates distance between two locations
    private fun fetchPair(from: Location, to: Location): MapboxRouteResponse? {
        // overview=false en geen geometries: we hebben alleen afstand en tijd nodig
        val url = "https://api.mapbox.com/directions/v5/mapbox/driving/" +
                  "${from.longitude},${from.latitude};" +
                  "${to.longitude},${to.latitude}" +
                  "?overview=false&access_token=$accessToken"
        return parseDirectionsResponse(url)
    }

    private fun parseDirectionsResponse(url: String): MapboxRouteResponse? {
        return try {
            val json   = JSONObject(URL(url).readText())
            val routes = json.getJSONArray("routes")
            if (routes.length() == 0) return null

            val first    = routes.getJSONObject(0)
            val distance = first.getDouble("distance")
            val duration = first.getDouble("duration")

            val coordsJson = first.optJSONObject("geometry")
                ?.optJSONArray("coordinates")

            val geometry = if (coordsJson != null) {
                (0 until coordsJson.length()).map { i ->
                    val pair = coordsJson.getJSONArray(i)
                    doubleArrayOf(pair.getDouble(0), pair.getDouble(1))
                }
            } else emptyList()

            MapboxRouteResponse(geometry, distance, duration)
        } catch (e: Exception) {
            null
        }
    }
}
