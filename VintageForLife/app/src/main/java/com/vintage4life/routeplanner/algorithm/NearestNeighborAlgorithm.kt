package com.vintage4life.routeplanner.algorithm

import com.vintage4life.routeplanner.distance.DistanceCalculator
import com.vintage4life.routeplanner.distance.DistanceMatrix
import com.vintage4life.routeplanner.model.Location
import com.vintage4life.routeplanner.model.Route

/**
 * Greedy Nearest Neighbor heuristiek voor TSP.
 *
 * Complexiteit: O(n²)
 * Start vanuit index 0 en bezoekt altijd de dichtstbijzijnde onbezochte stop.
 * Levert een geldige maar niet-geoptimaliseerde initiële route op.
 *
 * Conform UML: solve(List<Location>, DistanceCalculator): Route, buildRoute(...): Route
 */
class NearestNeighborAlgorithm : TSPAlgorithm {

    override fun solve(locations: List<Location>, calculator: DistanceCalculator): Route {
        val matrix  = DistanceMatrix(locations, calculator)
        val indices = solveIndices(matrix)
        return buildRoute(indices, matrix)
    }

    /**
     * Kern-algoritme: bepaalt de greedy volgorde als lijst van matrix-indices.
     * Intern gebruikt door [TwoOptAlgorithm] zodat de matrix niet opnieuw gebouwd hoeft.
     */
    fun solveIndices(matrix: DistanceMatrix): List<Int> {
        val n       = matrix.size()
        val visited = BooleanArray(n) { false }
        val route   = mutableListOf<Int>()

        var current = 0
        visited[current] = true
        route.add(current)

        repeat(n - 1) {
            var nearest     = -1
            var nearestCost = Double.MAX_VALUE

            for (next in 0 until n) {
                if (!visited[next]) {
                    val cost = matrix.distance(current, next)
                    if (cost < nearestCost) {
                        nearestCost = cost
                        nearest = next
                    }
                }
            }

            // BUG-FIX: guard tegen nearest == -1 (kan theoretisch niet, maar voorkomt crash)
            if (nearest == -1) return route

            visited[nearest] = true
            route.add(nearest)
            current = nearest
        }

        return route
    }

    /**
     * Zet een geordende lijst van indices om naar een [Route].
     * De `totalDistance` in het geretourneerde Route-object is de kostwaarde uit
     * de matrix (kan km, seconden of CO₂ zijn afhankelijk van de meegegeven calculator).
     * De [RoutePlannerService] overschrijft deze waarden met de definitieve metrics.
     * Conform UML: buildRoute(...): Route
     */
    fun buildRoute(indices: List<Int>, matrix: DistanceMatrix): Route {
        val orderedLocations = indices.map { matrix.locationAt(it) }

        // Tussenliggende segmenten
        val segmentCost = (0 until indices.size - 1).sumOf { i ->
            matrix.distance(indices[i], indices[i + 1])
        }
        // Terugrit naar startpunt (gesloten TSP: A→B→C→D→A)
        val returnCost = matrix.distance(indices.last(), indices.first())

        return Route(
            locations     = orderedLocations,
            totalDistance = segmentCost + returnCost
        )
    }
}
