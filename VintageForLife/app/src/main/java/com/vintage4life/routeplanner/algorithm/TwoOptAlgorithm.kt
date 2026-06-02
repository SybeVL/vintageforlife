package com.vintage4life.routeplanner.algorithm

import com.vintage4life.routeplanner.distance.DistanceMatrix

/**
 * Two-Opt local search improvement for TSP.
 *
 * Complexity: O(n²) per iteration, typically O(n³) worst case overall.
 * Takes an initial route (e.g. from NearestNeighborAlgorithm) and
 * iteratively reverses sub-segments when doing so reduces total distance.
 *
 * @param maxIterations  Safety cap on improvement iterations (default: 1000)
 */
class TwoOptAlgorithm(private val maxIterations: Int = 1000) : TSPAlgorithm {

    override fun solve(matrix: DistanceMatrix): List<Int> {
        // TwoOpt expects an initial route; when called standalone it falls back to 0..n-1
        val initial = (0 until matrix.size()).toMutableList()
        return improve(initial, matrix)
    }

    /**
     * Improve an existing route using Two-Opt swaps.
     * @param initialRoute  Route produced by a construction heuristic
     * @param matrix        Distance matrix
     * @return              Locally optimised route
     */
    fun improve(initialRoute: List<Int>, matrix: DistanceMatrix): List<Int> {
        val route = initialRoute.toMutableList()
        val n = route.size
        var improved = true
        var iterations = 0

        while (improved && iterations < maxIterations) {
            improved = false
            iterations++

            for (i in 0 until n - 1) {
                for (j in i + 2 until n) {
                    val delta = swapGain(route, i, j, matrix)
                    if (delta < -1e-10) {
                        // Reverse the segment between i+1 and j
                        reverseSegment(route, i + 1, j)
                        improved = true
                    }
                }
            }
        }

        return route
    }

    /** Calculates the distance gain from a 2-opt swap between edges (i, i+1) and (j, j+1). */
    private fun swapGain(route: List<Int>, i: Int, j: Int, matrix: DistanceMatrix): Double {
        val n = route.size
        val a = route[i]
        val b = route[i + 1]
        val c = route[j]
        val d = route[(j + 1) % n]

        val before = matrix.distance(a, b) + matrix.distance(c, d)
        val after  = matrix.distance(a, c) + matrix.distance(b, d)
        return after - before
    }

    private fun reverseSegment(route: MutableList<Int>, from: Int, to: Int) {
        var l = from
        var r = to
        while (l < r) {
            val tmp = route[l]
            route[l] = route[r]
            route[r] = tmp
            l++; r--
        }
    }
}
