package com.vintage4life.routeplanner.distance

import com.vintage4life.routeplanner.model.Location

// calculates CO2 values between locations a and b
class CO2Calculator(
    private val baseCalculator: DistanceCalculator = HaversineCalculator()
) : DistanceCalculator {
    override fun calculate(from: Location, to: Location): Double {
        val distance = baseCalculator.calculate(from, to)

        // short distances = higher emission. forces algorithm to take longer roads
        val efficientGramsPerKm = when {
            distance < 2.0  -> 250.0
            distance < 8.0  -> 150.0
            else            -> 90.0
        }

        return distance * efficientGramsPerKm
    }
}
