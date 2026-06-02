package com.vintage4life.routeplanner.model

/**
 * Represents a geographic stop on the route.
 * @param name       Optional human-readable label (e.g. "Klant A")
 * @param address    Full street address used for geocoding
 * @param latitude   WGS-84 latitude in decimal degrees
 * @param longitude  WGS-84 longitude in decimal degrees
 */
data class Location(
    val id: String,
    val name: String = "",
    val address: String,
    val latitude: Double,
    val longitude: Double
)
