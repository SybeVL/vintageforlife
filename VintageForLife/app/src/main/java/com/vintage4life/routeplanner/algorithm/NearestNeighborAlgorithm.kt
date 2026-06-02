package com.vintage4life.routeplanner.algorithm

import com.vintage4life.routeplanner.distance.DistanceMatrix

/**
 * Greedy Nearest Neighbor heuristic for TSP.
 *
 * Complexity: O(n²)
 * Starts from index 0, always visits the closest unvisited stop next.
 * Produces a valid but unoptimised initial route.
 */
class NearestNeighborAlgorithm : TSPAlgorithm {

    override fun solve(matrix: DistanceMatrix): List<Int> {
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
}
