package com.vintage4life.routeplanner.distance

import com.vintage4life.routeplanner.model.Location
import kotlin.math.*

// using haversine math formula. calculates distance using earth radius
class HaversineCalculator : DistanceCalculator {

    private val earthRadiusKm = 6371.0

    override fun calculate(from: Location, to: Location): Double {
        val lat1 = Math.toRadians(from.latitude)
        val lat2 = Math.toRadians(to.latitude)
        val dLat = Math.toRadians(to.latitude - from.latitude)
        val dLon = Math.toRadians(to.longitude - from.longitude)

        val a = sin(dLat / 2).pow(2) +
                cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadiusKm * c
    }
}
