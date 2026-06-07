package com.vintage4life.routeplanner.distance

import com.vintage4life.routeplanner.model.Location
import com.vintage4life.routeplanner.model.OptimizationCriteria

/**
 * Calculates a weighted cost between two locations based on the optimization criteria.
 *
 * Implements [DistanceCalculator] so it can be used by [DistanceMatrix] to build
 * cost matrices that the TSP algorithms (NearestNeighbor, TwoOpt) can solve.
 */
class CostCalculator(
    private val criteria: OptimizationCriteria,
    private val baseCalculator: DistanceCalculator = HaversineCalculator()
) : DistanceCalculator {

    /**
     * Calculates the cost (distance, time, or CO2) between two locations.
     * Used by the algorithms to find the "cheapest" path.
     */
    override fun calculate(from: Location, to: Location): Double {
        val distanceKm = baseCalculator.calculate(from, to)
        
        return when (criteria) {
            OptimizationCriteria.DISTANCE -> distanceKm
            
            OptimizationCriteria.TIME -> {
                // Simplified time model: 50 km/h average
                distanceKm / 50.0 
            }
            
            OptimizationCriteria.SUSTAINABILITY -> {
                // Simplified CO2 model: 200g per km
                distanceKm * 0.200
            }
        }
    }
}
