package com.vintage4life.routeplanner.algorithm

import com.vintage4life.routeplanner.distance.DistanceCalculator
import com.vintage4life.routeplanner.distance.DistanceMatrix
import com.vintage4life.routeplanner.model.Location
import com.vintage4life.routeplanner.model.Route

/**
 * Multi-start 2-opt for closed TSP (A→B→C→D→A).
 * Runs NearestNeighbor from every start node and keeps the best 2-opt result.
 */
class TwoOptAlgorithm(
    private val initializer: NearestNeighborAlgorithm = NearestNeighborAlgorithm(),
    private val maxIterations: Int = 1000
) : TSPAlgorithm {

    override fun solve(locations: List<Location>, calculator: DistanceCalculator): Route {
        val matrix = DistanceMatrix(locations, calculator)
        val n      = matrix.size()

        var bestIndices = emptyList<Int>()
        var bestCost    = Double.MAX_VALUE

        // Trying every start node avoids local minima (ensures DISTANCE ≤ TIME in km)
        for (start in 0 until n) {
            val initial  = initializer.solveIndices(matrix, startFrom = start)
            val improved = improve(initial, matrix)
            val cost     = routeCost(improved, matrix)

            if (cost < bestCost) {
                bestCost    = cost
                bestIndices = improved
            }
        }

        return initializer.buildRoute(bestIndices, matrix)
    }

    // Repeatedly applies 2-opt swaps until no further improvement is found.
    fun improve(initialRoute: List<Int>, matrix: DistanceMatrix): List<Int> {
        val route = initialRoute.toMutableList()
        val n     = route.size
        var improved   = true
        var iterations = 0

        while (improved && iterations < maxIterations) {
            improved = false
            iterations++

            for (i in 0 until n - 1) {
                for (j in i + 2 until n) {
                    val delta = swapGain(route, i, j, matrix)
                    if (delta < -1e-10) {
                        reverse(route, i + 1, j)
                        improved = true
                    }
                }
            }
        }

        return route
    }

    // Total route cost including the return leg to the start node.
    private fun routeCost(indices: List<Int>, matrix: DistanceMatrix): Double {
        val segmentCost = (0 until indices.size - 1).sumOf { i ->
            matrix.distance(indices[i], indices[i + 1])
        }
        return segmentCost + matrix.distance(indices.last(), indices.first())
    }

    // Returns the cost delta of a 2-opt swap; negative means an improvement.
    private fun swapGain(route: List<Int>, i: Int, j: Int, matrix: DistanceMatrix): Double {
        val n = route.size
        val a = route[i];           val b = route[i + 1]
        val c = route[j];           val d = route[(j + 1) % n]  // cyclic wrap for closed route

        return (matrix.distance(a, c) + matrix.distance(b, d)) -
               (matrix.distance(a, b) + matrix.distance(c, d))
    }

    // Reverses the segment [from to] of the route in-place.
    private fun reverse(route: MutableList<Int>, from: Int, to: Int) {
        var l = from; var r = to
        while (l < r) {
            val tmp = route[l]; route[l] = route[r]; route[r] = tmp
            l++; r--
        }
    }
}
