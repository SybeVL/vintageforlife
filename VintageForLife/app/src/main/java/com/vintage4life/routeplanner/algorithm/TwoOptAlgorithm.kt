package com.vintage4life.routeplanner.algorithm

import com.vintage4life.routeplanner.distance.DistanceCalculator
import com.vintage4life.routeplanner.distance.DistanceMatrix
import com.vintage4life.routeplanner.model.Location
import com.vintage4life.routeplanner.model.Route

/**
 * Two-Opt lokale zoekverbetering voor gesloten TSP (A→B→C→D→A).
 *
 * Complexiteit: O(n²) per iteratie, worst case O(n³).
 * Gebruikt [NearestNeighborAlgorithm] voor de initiële route en verbetert
 * deze iteratief via segment-omkeringen.
 *
 * De route is GESLOTEN: na de laatste stop keert de chauffeur terug naar de
 * eerste stop. swapGain houdt hier rekening mee via de cyclische wrap.
 */
class TwoOptAlgorithm(
    private val initializer: NearestNeighborAlgorithm = NearestNeighborAlgorithm(),
    private val maxIterations: Int = 1000
) : TSPAlgorithm {

    override fun solve(locations: List<Location>, calculator: DistanceCalculator): Route {
        val matrix          = DistanceMatrix(locations, calculator)
        val initialIndices  = initializer.solveIndices(matrix)
        val improvedIndices = improve(initialIndices, matrix)
        return initializer.buildRoute(improvedIndices, matrix)
    }

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
                        reverse(route, i + 1, j)
                        improved = true
                    }
                }
            }
        }

        return route
    }

    /**
     * Berekent de winst van een 2-opt swap voor een GESLOTEN route.
     * route[0] is de wrap-around: na de laatste stop keer je terug naar de start.
     */
    private fun swapGain(route: List<Int>, i: Int, j: Int, matrix: DistanceMatrix): Double {
        val n = route.size
        val a = route[i]
        val b = route[i + 1]
        val c = route[j]
        val d = route[(j + 1) % n]   // cyclische wrap voor gesloten rondrit

        return (matrix.distance(a, c) + matrix.distance(b, d)) -
               (matrix.distance(a, b) + matrix.distance(c, d))
    }

    private fun reverse(route: MutableList<Int>, from: Int, to: Int) {
        var l = from; var r = to
        while (l < r) {
            val tmp = route[l]; route[l] = route[r]; route[r] = tmp
            l++; r--
        }
    }
}
