package com.vintage4life.routeplanner.distance

import com.vintage4life.routeplanner.model.Location

/**
 * Strategy for computing CO2 emissions between two locations.
 * Minimal implementation using distance and an emission factor.
 */
class CO2Calculator(
    private val baseCalculator: DistanceCalculator = HaversineCalculator()
) : DistanceCalculator {
    override fun calculate(from: Location, to: Location): Double {
        val distance = baseCalculator.calculate(from, to)

        // Aggressive non-linearity: simulate extreme inefficiency for very short city hops.
        // This encourages the algorithm to combine short segments into longer ones if possible.
        val efficientGramsPerKm = when {
            distance < 2.0  -> 250.0
            distance < 8.0  -> 150.0
            else            -> 90.0
        }

        return distance * efficientGramsPerKm
    }
}
