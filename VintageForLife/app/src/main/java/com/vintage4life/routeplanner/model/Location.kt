package com.vintage4life.routeplanner.model

import kotlin.math.*

/**
 * Represents a geographic stop on the route.
 *
 * UML-afwijking: UML toont velden [x, y: double].
 * Geïmplementeerd als [latitude, longitude] omdat de Android Geocoder
 * en Mapbox GPS-locaties WGS-84 coördinaten leveren.
 * Functioneel equivalent — x = longitude, y = latitude.
 *
 * @param id         Uniek stop-ID
 * @param name       Optioneel leesbaar label (bijv. "Klant A")
 * @param address    Volledig straatadres voor geocoding
 * @param latitude   WGS-84 breedtegraad in decimale graden (= UML y)
 * @param longitude  WGS-84 lengtegraad in decimale graden (= UML x)
 */
data class Location(
    val id: String,
    val name: String = "",
    val address: String,
    val latitude: Double,
    val longitude: Double
) {
    /**
     * Berekent de Haversine-afstand (luchtvogel) tot een andere locatie in km.
     * Conform UML: distanceTo(Location): double
     */
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
