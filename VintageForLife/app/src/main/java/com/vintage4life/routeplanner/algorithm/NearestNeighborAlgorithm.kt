package com.vintage4life.routeplanner.algorithm

import com.vintage4life.routeplanner.distance.DistanceCalculator
import com.vintage4life.routeplanner.distance.DistanceMatrix
import com.vintage4life.routeplanner.model.Location
import com.vintage4life.routeplanner.model.Route

/**
 * Greedy Nearest Neighbor heuristiek voor TSP.
 *
 * Complexiteit: O(n²)
 * Bezoekt altijd de dichtstbijzijnde onbezochte stop.
 * Levert een geldige maar niet-geoptimaliseerde initiële route op.
 *
 * Conform UML: solve(List<Location>, DistanceCalculator): Route, buildRoute(...): Route
 */
class NearestNeighborAlgorithm : TSPAlgorithm {

    override fun solve(locations: List<Location>, calculator: DistanceCalculator): Route {
        val matrix  = DistanceMatrix(locations, calculator)
        val indices = solveIndices(matrix, startFrom = 0)
        return buildRoute(indices, matrix)
    }

    /**
     * Greedy volgorde als lijst van indices, startend vanuit [startFrom].
     * Intern gebruikt door [TwoOptAlgorithm] voor multi-start optimalisatie.
     */
    fun solveIndices(matrix: DistanceMatrix, startFrom: Int = 0): List<Int> {
        val n       = matrix.size()
        val visited = BooleanArray(n) { false }
        val route   = mutableListOf<Int>()

        var current = startFrom
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
                        nearest     = next
                    }
                }
            }

            if (nearest == -1) return route

            visited[nearest] = true
            route.add(nearest)
            current = nearest
        }

        return route
    }

    /**
     * Zet indices om naar een [Route] inclusief terugrit (gesloten TSP).
     * Conform UML: buildRoute(...): Route
     */
    fun buildRoute(indices: List<Int>, matrix: DistanceMatrix): Route {
        val orderedLocations = indices.map { matrix.locationAt(it) }
        val segmentCost = (0 until indices.size - 1).sumOf { i ->
            matrix.distance(indices[i], indices[i + 1])
        }
        val returnCost = matrix.distance(indices.last(), indices.first())
        return Route(
            locations     = orderedLocations,
            totalDistance = segmentCost + returnCost
        )
    }
}
