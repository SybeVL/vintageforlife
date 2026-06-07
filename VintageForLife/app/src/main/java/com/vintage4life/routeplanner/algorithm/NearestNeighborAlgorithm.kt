package com.vintage4life.routeplanner.algorithm

import android.util.Log
import com.vintage4life.routeplanner.distance.CostCalculator
import com.vintage4life.routeplanner.distance.DistanceMatrix
import com.vintage4life.routeplanner.distance.HaversineCalculator
import com.vintage4life.routeplanner.model.Location
import com.vintage4life.routeplanner.model.OptimizationCriteria
import com.vintage4life.routeplanner.model.Route

/**
 * Greedy Nearest Neighbor heuristiek voor TSP.
 */
class NearestNeighborAlgorithm : TSPAlgorithm {

    private val TAG = "NearestNeighbor"

    /**
     * Berekent een greedy route langs alle stops.
     */
    override fun solve(locations: List<Location>, criteria: OptimizationCriteria): Route {
        Log.d(TAG, "Starting Nearest Neighbor for ${locations.size} stops (Criteria: $criteria)")
        // Gebruik de CostCalculator om de matrix te vullen op basis van het criterium
        val matrix = DistanceMatrix(locations, CostCalculator(criteria))
        val indices = solveIndices(matrix)
        val route = buildRoute(indices, matrix, criteria)
        Log.d(TAG, "Initial route distance: ${"%.2f".format(route.totalDistance)} km")
        return route
    }

    /**
     * Kern-algoritme: werkt op indices van de afstandsmatrix.
     */
    fun solveIndices(matrix: DistanceMatrix): List<Int> {
        val n = matrix.size()
        val visited = BooleanArray(n) { false }
        val route = mutableListOf<Int>()

        var current = 0
        visited[current] = true
        route.add(current)

        repeat(n - 1) {
            var nearest = -1
            var nearestCost = Double.MAX_VALUE

            for (next in 0 until n) {
                if (!visited[next]) {
                    // Dit is nu de 'kosten' (km, tijd of CO2) afhankelijk van de matrix-opbouw
                    val cost = matrix.distance(current, next)
                    if (cost < nearestCost) {
                        nearestCost = cost
                        nearest = next
                    }
                }
            }

            if (nearest != -1) {
                visited[nearest] = true
                route.add(nearest)
                current = nearest
            }
        }

        return route
    }

    /**
     * Zet een geordende lijst van indices om naar een [Route].
     */
    fun buildRoute(indices: List<Int>, matrix: DistanceMatrix, criteria: OptimizationCriteria): Route {
        val orderedLocations = indices.map { matrix.locationAt(it) }
        
        // Bereken de daadwerkelijke KM's voor de resultaatweergave
        val haversine = HaversineCalculator()
        val totalDistance = (0 until orderedLocations.size - 1).sumOf { i ->
            haversine.calculate(orderedLocations[i], orderedLocations[i + 1])
        }
        
        // Bereken tijd op basis van KM's
        val estimatedTime = (totalDistance / 50.0) * 60.0

        return Route(
            locations     = orderedLocations,
            totalDistance = totalDistance,
            estimatedTimeMin = estimatedTime,
            criteria = criteria
        )
    }
}
