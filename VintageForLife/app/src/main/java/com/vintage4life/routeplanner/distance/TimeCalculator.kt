package com.vintage4life.routeplanner.distance

import com.vintage4life.routeplanner.model.Location

// uses average speed per km tied to the distance to travel in km to calculate time
class TimeCalculator(
    private val baseCalculator: DistanceCalculator = HaversineCalculator()
) : DistanceCalculator {

    override fun calculate(from: Location, to: Location): Double {
        val distanceKm = baseCalculator.calculate(from, to)

        val speedKmh = when {
            distanceKm < 2.0  -> 30.0
            distanceKm < 15.0 -> 50.0
            else              -> 80.0
        }

        return distanceKm / speedKmh  // uren
    }
}
