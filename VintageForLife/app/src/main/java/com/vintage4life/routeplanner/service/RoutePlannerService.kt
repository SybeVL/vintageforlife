package com.vintage4life.routeplanner.service

import com.vintage4life.routeplanner.algorithm.TSPAlgorithm
import com.vintage4life.routeplanner.algorithm.TwoOptAlgorithm
import com.vintage4life.routeplanner.distance.CO2Calculator
import com.vintage4life.routeplanner.distance.HaversineCalculator
import com.vintage4life.routeplanner.distance.PrecomputedCalculator
import com.vintage4life.routeplanner.distance.RoadMatrix
import com.vintage4life.routeplanner.distance.TimeCalculator
import com.vintage4life.routeplanner.model.Location
import com.vintage4life.routeplanner.model.OptimizationCriteria
import com.vintage4life.routeplanner.model.Route

/**
 * Orchestrates route optimisation using a TSP algorithm and real road data.
 * Falls back to Haversine/Time/CO2 calculators when no [RoadMatrix] is available.
 */
class RoutePlannerService(
    private var algorithm: TSPAlgorithm = TwoOptAlgorithm()
) {

    fun planRoute(
        stops: List<Location>,
        criteria: OptimizationCriteria,
        roadMatrix: RoadMatrix? = null
    ): Route {
        require(stops.size >= 2) { "At least 2 stops required." }

        // Pick the cost calculator based on criteria and available road data
        val optimizationCalculator = if (roadMatrix != null) {
            val costMatrix = when (criteria) {
                OptimizationCriteria.DISTANCE -> roadMatrix.distancesKm
                OptimizationCriteria.TIME     -> roadMatrix.durationsSeconds
                OptimizationCriteria.SUSTAINABILITY -> Array(stops.size) { i ->
                    DoubleArray(stops.size) { j ->
                        roadMatrix.distancesKm[i][j] * co2GramsPerKm(roadMatrix.distancesKm[i][j])
                    }
                }
            }
            PrecomputedCalculator(stops, costMatrix)
        } else {
            // Fallback when Directions API is unavailable — less accurate
            when (criteria) {
                OptimizationCriteria.DISTANCE       -> HaversineCalculator()
                OptimizationCriteria.TIME           -> TimeCalculator()
                OptimizationCriteria.SUSTAINABILITY -> CO2Calculator()
            }
        }

        // Solve the TSP to get the optimal route order
        val optimizedRoute = algorithm.solve(stops, optimizationCalculator)

        // Compute display metrics over all segments including the return leg (closed TSP)
        var totalDistKm   = 0.0
        var totalTimeMin  = 0.0
        var totalCO2Grams = 0.0

        val locs = optimizedRoute.locations
        val segments = (0 until locs.size - 1).map { locs[it] to locs[it + 1] } +
                       listOf(locs.last() to locs.first()) // Add return route for closed loop

        val haversine = HaversineCalculator()
        val timeCalc  = TimeCalculator()
        val co2Calc   = CO2Calculator()

        for ((from, to) in segments) {
            if (roadMatrix != null) {
                val fi = stops.indexOfFirst { it.id == from.id }
                val ti = stops.indexOfFirst { it.id == to.id }
                if (fi >= 0 && ti >= 0) {
                    val distKm = roadMatrix.distancesKm[fi][ti]
                    totalDistKm   += distKm
                    totalTimeMin  += roadMatrix.durationsSeconds[fi][ti] / 60.0
                    totalCO2Grams += distKm * co2GramsPerKm(distKm)
                }
            } else {
                val dist = haversine.calculate(from, to)
                totalDistKm   += dist
                totalTimeMin  += timeCalc.calculate(from, to) * 60.0
                totalCO2Grams += co2Calc.calculate(from, to)
            }
        }

        return optimizedRoute.copy(
            totalDistance    = totalDistKm,
            estimatedTimeMin = totalTimeMin,
            totalCO2Grams    = totalCO2Grams,
            criteria         = criteria
        )
    }

    // Swaps the active TSP algorithm at runtime.
    fun setAlgorithm(newAlgorithm: TSPAlgorithm) {
        algorithm = newAlgorithm
    }

    // CO2 emission factor (g/km) scaled by typical trip distance ranges.
    private fun co2GramsPerKm(distKm: Double): Double = when {
        distKm < 2.0 -> 220.0
        distKm < 8.0 -> 160.0
        else         -> 110.0
    }
}
