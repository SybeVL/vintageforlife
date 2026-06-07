package com.vintage4life.routeplanner.algorithm

import com.vintage4life.routeplanner.distance.DistanceCalculator
import com.vintage4life.routeplanner.distance.DistanceMatrix
import com.vintage4life.routeplanner.model.Location
import com.vintage4life.routeplanner.model.Route

/**
 * Two-Opt lokale zoekverbetering voor TSP.
 *
 * Complexiteit: O(n²) per iteratie, worst case O(n³).
 * Gebruikt [NearestNeighborAlgorithm] voor de initiële route
 * en verbetert deze iteratief via segment-omkeringen.
 *
 * Conform UML:
 *  - solve(List<Location>): Route
 *  - reverse(...): void  (hernoemd van reverseSegment)
 *  - Afhankelijkheid van NearestNeighborAlgorithm (UML-pijl)
 *
 * @param initializer    Constructie-heuristiek voor de beginroute
 * @param maxIterations  Veiligheidsgrens voor verbeteriteraties
 */
class TwoOptAlgorithm(
    private val initializer: NearestNeighborAlgorithm = NearestNeighborAlgorithm(),
    private val maxIterations: Int = 1000
) : TSPAlgorithm {

    /**
     * Berekent een geoptimaliseerde route via NearestNeighbor + 2-opt.
     * Conform UML: solve(List<Location>): Route
     */
    override fun solve(locations: List<Location>, calculator: DistanceCalculator): Route {
        val matrix          = DistanceMatrix(locations, calculator)
        val initialIndices  = initializer.solveIndices(matrix)
        val improvedIndices = improve(initialIndices, matrix)

        return initializer.buildRoute(improvedIndices, matrix)
    }

    /**
     * Verbetert een bestaande route via 2-opt swaps.
     * Blijft intern bruikbaar door [RoutePlannerService].
     *
     * @param initialRoute  Route van een constructie-heuristiek (als indices)
     * @param matrix        Afstandsmatrix
     * @return              Lokaal geoptimaliseerde route als indices
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
        val a = route[i]
        val b = route[i + 1]
        val c = route[j]
        val d = if (j + 1 < n) route[j + 1] else route[0]

        // 2-opt gain: (dist(a,c) + dist(b,d)) - (dist(a,b) + dist(c,d))
        return (matrix.distance(a, c) + matrix.distance(b, d)) -
               (matrix.distance(a, b) + matrix.distance(c, d))
    }

    /**
     * Keert het segment [from..to] in de route om.
     * Conform UML: reverse(...): void
     */
    private fun reverse(route: MutableList<Int>, from: Int, to: Int) {
        var l = from; var r = to
        while (l < r) {
            val tmp = route[l]; route[l] = route[r]; route[r] = tmp
            l++; r--
        }
    }
}
