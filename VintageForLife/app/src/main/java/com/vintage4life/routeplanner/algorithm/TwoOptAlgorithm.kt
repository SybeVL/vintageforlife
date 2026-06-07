package com.vintage4life.routeplanner.algorithm

import android.util.Log
import com.vintage4life.routeplanner.distance.CostCalculator
import com.vintage4life.routeplanner.distance.DistanceMatrix
import com.vintage4life.routeplanner.model.Location
import com.vintage4life.routeplanner.model.OptimizationCriteria
import com.vintage4life.routeplanner.model.Route

/**
 * Two-Opt lokale zoekverbetering voor TSP.
 */
class TwoOptAlgorithm(
    private val initializer: NearestNeighborAlgorithm = NearestNeighborAlgorithm(),
    private val maxIterations: Int = 1000
) : TSPAlgorithm {

    private val TAG = "TwoOptAlgorithm"

    /**
     * Berekent een geoptimaliseerde route via NearestNeighbor + 2-opt.
     */
    override fun solve(locations: List<Location>, criteria: OptimizationCriteria): Route {
        Log.d(TAG, "Starting 2-opt optimization (Criteria: $criteria)")
        // De matrix bevat de 'kosten' (km, tijd, co2) op basis van het criterium
        val matrix          = DistanceMatrix(locations, CostCalculator(criteria))
        val initialIndices  = initializer.solveIndices(matrix)
        
        val initialCost = (0 until initialIndices.size - 1).sumOf { i -> matrix.distance(initialIndices[i], initialIndices[i+1]) }
        Log.d(TAG, "Initial NearestNeighbor cost: ${"%.4f".format(initialCost)}")

        val improvedIndices = improve(initialIndices, matrix)
        
        val finalCost = (0 until improvedIndices.size - 1).sumOf { i -> matrix.distance(improvedIndices[i], improvedIndices[i+1]) }
        Log.d(TAG, "Final 2-opt cost: ${"%.4f".format(finalCost)}")

        return initializer.buildRoute(improvedIndices, matrix, criteria)
    }

    /**
     * Verbetert een bestaande route via 2-opt swaps gebaseerd op de kosten in de matrix.
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
                    // Als delta negatief is, is de nieuwe route 'goedkoper'
                    if (delta < -1e-10) {
                        reverse(route, i + 1, j)
                        improved = true
                    }
                }
            }
        }

        return route
    }

    private fun swapGain(route: List<Int>, i: Int, j: Int, matrix: DistanceMatrix): Double {
        val n = route.size
        val a = route[i];      val b = route[i + 1]
        val c = route[j];      val d = route[(j + 1) % n]
        
        // Bereken winst gebaseerd op kosten in de matrix
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
