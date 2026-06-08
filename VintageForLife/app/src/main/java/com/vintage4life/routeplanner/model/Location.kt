package com.vintage4life.routeplanner.model

import kotlin.math.*

// Represents a geographic stop on the route (latitude = UML y, longitude = UML x).
data class Location(
    val id: String,
    val name: String = "",
    val address: String,
    val latitude: Double,
    val longitude: Double
) {
    // Returns the Haversine (straight-line) distance to [other] in km.
    fun distanceTo(other: Location): Double {
        val earthRadius = 6371.0
        val lat1 = Math.toRadians(latitude)
        val lat2 = Math.toRadians(other.latitude)
        val dLat = Math.toRadians(other.latitude - latitude)
        val dLon = Math.toRadians(other.longitude - longitude)

        val a = sin(dLat / 2).pow(2) +
                cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }
}
