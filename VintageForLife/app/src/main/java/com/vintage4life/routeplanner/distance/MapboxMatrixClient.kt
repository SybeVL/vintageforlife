package com.vintage4life.routeplanner.distance

import com.vintage4life.routeplanner.model.Location
import org.json.JSONObject
import java.net.URL

/**
 * Haalt een N×N rijtijd- en wegafstandsmatrix op via de Mapbox Matrix API.
 *
 * Eén API-call levert alle combinaties van stops terug, zodat het TSP-algoritme
 * op echte wegdata kan optimaliseren in plaats van luchtvogel-afstanden.
 *
 * Endpoint: GET /directions-matrix/v1/mapbox/driving/{coords}
 * Maximaal 25 locaties per request.
 *
 * Blokkerende aanroep — altijd aanroepen vanuit een achtergrondthread.
 */
class MapboxMatrixClient(private val accessToken: String) {

    /**
     * Haalt de volledige N×N matrix op voor de gegeven stops.
     * Retourneert null bij een fout of lege response (fallback naar Haversine).
     */
    fun fetchMatrix(locations: List<Location>): RoadMatrix? {
        if (locations.size < 2) return null

        val coords = locations.joinToString(";") { "${it.longitude},${it.latitude}" }
        val url = "https://api.mapbox.com/directions-matrix/v1/mapbox/driving/$coords" +
                  "?annotations=distance,duration&access_token=$accessToken"

        return try {
            val json      = JSONObject(URL(url).readText())
            val durationJson  = json.optJSONArray("durations")  ?: return null
            val distanceJson  = json.optJSONArray("distances")  ?: return null
            val n = locations.size

            val durations = Array(n) { i ->
                DoubleArray(n) { j ->
                    durationJson.getJSONArray(i).optDouble(j, Double.MAX_VALUE / 2)
                }
            }
            val distances = Array(n) { i ->
                DoubleArray(n) { j ->
                    // API geeft meters terug → omrekenen naar km
                    distanceJson.getJSONArray(i).optDouble(j, Double.MAX_VALUE / 2) / 1000.0
                }
            }

            RoadMatrix(durations, distances)
        } catch (e: Exception) {
            null  // val terug op Haversine in de service
        }
    }
}
