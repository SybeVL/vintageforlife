package com.vintage4life.routeplanner.distance

import com.vintage4life.routeplanner.model.Location
import org.json.JSONObject
import java.net.URL

/**
 * Resultaat van de Mapbox Directions API voor een route.
 */
data class MapboxRouteResponse(
    val geometry: List<DoubleArray>,
    val distanceMeters: Double,
    val durationSeconds: Double
)

/**
 * Haalt echte wegdata op via de Mapbox Directions REST API.
 * Blokkerende aanroep — altijd aanroepen vanuit een achtergrondthread.
 */
class MapboxDirectionsClient(private val accessToken: String) {

    /**
     * Haalt routegeometrie en metrics op voor een geordende lijst van stops.
     * Wordt gebruikt om de routelijn op de kaart te tekenen.
     */
    fun fetchRouteData(stops: List<Location>): MapboxRouteResponse? {
        if (stops.size < 2) return null
        val coords = stops.joinToString(";") { "${it.longitude},${it.latitude}" }
        val url = "https://api.mapbox.com/directions/v5/mapbox/driving/$coords" +
                  "?geometries=geojson&overview=full&access_token=$accessToken"
        return parseDirectionsResponse(url)
    }

    /**
     * Bouwt een N×N [RoadMatrix] door voor elk koppel stops de Directions API aan
     * te roepen. Gebruikt N×(N-1)/2 calls (gesymmetriseerd).
     *
     * Voor 6 stops = 15 calls. Alternatief voor de Mapbox Matrix API.
     */
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

    /**
     * Haalt afstand en rijtijd op tussen precies twee locaties.
     */
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
