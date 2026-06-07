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
 * Conform UML: solve(List<Location>): Route, buildRoute(...): Route
 */
class NearestNeighborAlgorithm : TSPAlgorithm {

    /**
     * Berekent een greedy route langs alle stops.
     * Conform UML: solve(List<Location>): Route
     */
    override fun solve(locations: List<Location>, calculator: DistanceCalculator): Route {
        val matrix = DistanceMatrix(locations, calculator)
        val indices = solveIndices(matrix)
        return buildRoute(indices, matrix)
    }

    /**
     * Kern-algoritme: werkt op indices van de afstandsmatrix.
     * Intern hulpmiddel voor [TwoOptAlgorithm] die de matrix hergebruikt.
     * Conform UML: onderdeel van buildRoute-verantwoordelijkheid.
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
            var nearestDist = Double.MAX_VALUE

            for (next in 0 until n) {
                if (!visited[next]) {
                    val dist = matrix.distance(current, next)
                    if (dist < nearestDist) {
                        nearestDist = dist
                        nearest = next
                    }
                }
            }

            visited[nearest] = true
            route.add(nearest)
            current = nearest
        }

        return route
    }

    /**
     * Zet een geordende lijst van indices om naar een [Route].
     * Berekent ook de totale afstand.
     * Conform UML: buildRoute(...): Route
     */
    fun buildRoute(indices: List<Int>, matrix: DistanceMatrix): Route {
        val orderedLocations = indices.map { matrix.locationAt(it) }
        val totalDistance = (0 until indices.size - 1).sumOf { i ->
            matrix.distance(indices[i], indices[i + 1])
        }
        return Route(
            locations     = orderedLocations,
            totalDistance = totalDistance
        )
    }
}
