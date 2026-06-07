package com.vintage4life.routeplanner.distance

import com.vintage4life.routeplanner.model.Location

/**
 * Strategy for computing travel time between two locations.
 * Minimal implementation using distance and an average speed.
 */
class TimeCalculator(
    private val baseCalculator: DistanceCalculator = HaversineCalculator()
) : DistanceCalculator {
    override fun calculate(from: Location, to: Location): Double {
        val distance = baseCalculator.calculate(from, to)

        // More aggressive non-linearity to force different routes even at short distances.
        // Short trips (< 3km) are very slow (15 km/h). 
        // Medium trips (3-10km) are moderate (40 km/h).
        // Long trips (> 10km) are fast (80 km/h).
        val averageSpeedKmh = when {
            distance < 3.0  -> 15.0
            distance < 10.0 -> 40.0
            else            -> 80.0
        }

        return distance / averageSpeedKmh
    }
}
