package com.vintage4life.routeplanner.algorithm

import com.vintage4life.routeplanner.distance.DistanceCalculator
import com.vintage4life.routeplanner.distance.DistanceMatrix
import com.vintage4life.routeplanner.model.Location
import com.vintage4life.routeplanner.model.Route

// Greedy Nearest Neighbor heuristic — O(n²). Produces the initial route for 2-opt.
class NearestNeighborAlgorithm : TSPAlgorithm {

    override fun solve(locations: List<Location>, calculator: DistanceCalculator): Route {
        val matrix  = DistanceMatrix(locations, calculator)
        val indices = solveIndices(matrix, startFrom = 0)
        return buildRoute(indices, matrix)
    }

    // Builds a greedy index order starting from [startFrom]. Used by multi-start 2-opt.
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

    // Converts indices to a Route including the return leg (closed TSP: A→…→A).
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
