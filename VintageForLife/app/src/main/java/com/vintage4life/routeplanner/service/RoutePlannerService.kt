package com.vintage4life.routeplanner.service

import com.vintage4life.routeplanner.algorithm.NearestNeighborAlgorithm
import com.vintage4life.routeplanner.algorithm.TwoOptAlgorithm
import com.vintage4life.routeplanner.distance.DistanceMatrix
import com.vintage4life.routeplanner.distance.HaversineCalculator
import com.vintage4life.routeplanner.model.Location
import com.vintage4life.routeplanner.model.OptimizationCriteria
import com.vintage4life.routeplanner.model.Route

/**
 * Core service that orchestrates route optimisation.
 *
 * Flow:
 *  1. Build DistanceMatrix using HaversineCalculator  — O(n²)
 *  2. Run NearestNeighborAlgorithm for initial route  — O(n²)
 *  3. Improve with TwoOptAlgorithm                   — O(n²–n³)
 *  4. Map index list back to Location objects
 */
class RoutePlannerService(
    private val calculator: HaversineCalculator = HaversineCalculator(),
    private val nearestNeighbor: NearestNeighborAlgorithm = NearestNeighborAlgorithm(),
    private val twoOpt: TwoOptAlgorithm = TwoOptAlgorithm()
) {

    /**
     * Optimise a list of stops for the given [criteria].
     * @throws IllegalArgumentException when fewer than 2 stops are provided.
     */
    fun optimizeRoute(stops: List<Location>, criteria: OptimizationCriteria): Route {
        require(stops.size >= 2) { "At least 2 stops are required to calculate a route." }

        // Step 1 – distance matrix
        val matrix = DistanceMatrix(stops, calculator)

        // Step 2 – greedy construction
        val initialRoute = nearestNeighbor.solve(matrix)

        // Step 3 – local improvement
        val optimizedIndices = twoOpt.improve(initialRoute, matrix)

        // Step 4 – map indices to Location objects
        val orderedStops = optimizedIndices.map { matrix.locationAt(it) }

        // Calculate total distance
        val totalDistance = (0 until optimizedIndices.size - 1).sumOf { i ->
            matrix.distance(optimizedIndices[i], optimizedIndices[i + 1])
        }

        // Rough time estimate: avg 50 km/h in urban delivery
        val estimatedTime = (totalDistance / 50.0) * 60.0

        return Route(
            stops = orderedStops,
            totalDistanceKm = totalDistance,
            estimatedTimeMin = estimatedTime,
            criteria = criteria
        )
    }
}
