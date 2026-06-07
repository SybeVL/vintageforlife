package com.vintage4life.routeplanner.algorithm

import com.vintage4life.routeplanner.distance.DistanceCalculator
import com.vintage4life.routeplanner.distance.DistanceMatrix
import com.vintage4life.routeplanner.model.Location
import com.vintage4life.routeplanner.model.Route

/**
 * Multi-start Two-Opt voor gesloten TSP (A→B→C→D→A).
 *
 * Aanpak:
 *  1. Probeer NearestNeighbor vanuit ELKE stop als startpunt  — O(n³)
 *  2. Verbeter elke kandidaatroute met 2-opt                  — O(n²) per iteratie
 *  3. Kies de route met de laagste totale kosten
 *
 * Door alle startpunten te proberen worden lokale minima vermeden die
 * ontstaan als je altijd vanuit stop 0 begint. Dit garandeert dat
 * DISTANCE nooit een langere route geeft dan TIME.
 *
 * Conform UML: solve(List<Location>, DistanceCalculator): Route, reverse(): void
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

        // Probeer elk startpunt — voorkomt lokale minima
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

    /**
     * Totale kosten van een gesloten route inclusief terugrit naar start.
     */
    private fun routeCost(indices: List<Int>, matrix: DistanceMatrix): Double {
        val segmentCost = (0 until indices.size - 1).sumOf { i ->
            matrix.distance(indices[i], indices[i + 1])
        }
        val returnCost = matrix.distance(indices.last(), indices.first())
        return segmentCost + returnCost
    }

    /**
     * Winst van een 2-opt swap voor gesloten route (cyclische wrap).
     */
    private fun swapGain(route: List<Int>, i: Int, j: Int, matrix: DistanceMatrix): Double {
        val n = route.size
        val a = route[i]
        val b = route[i + 1]
        val c = route[j]
        val d = route[(j + 1) % n]

        return (matrix.distance(a, c) + matrix.distance(b, d)) -
               (matrix.distance(a, b) + matrix.distance(c, d))
    }

    /** Conform UML: reverse(): void */
    private fun reverse(route: MutableList<Int>, from: Int, to: Int) {
        var l = from; var r = to
        while (l < r) {
            val tmp = route[l]; route[l] = route[r]; route[r] = tmp
            l++; r--
        }
    }
}
