package com.vintage4life.routeplanner.service

import com.vintage4life.routeplanner.algorithm.TSPAlgorithm
import com.vintage4life.routeplanner.algorithm.TwoOptAlgorithm
import com.vintage4life.routeplanner.distance.DistanceCalculator
import com.vintage4life.routeplanner.distance.HaversineCalculator
import com.vintage4life.routeplanner.model.Location
import com.vintage4life.routeplanner.model.OptimizationCriteria
import com.vintage4life.routeplanner.model.Route

/**
 * Kernservice die route-optimalisatie orkestreert.
 *
 * Conform UML:
 *  - velden: [algorithm: TSPAlgorithm], [calculator: DistanceCalculator]
 *  - methoden: [planRoute()], [setAlgorithm()]
 *
 * Het algoritme is uitwisselbaar via [setAlgorithm()] (Strategy pattern).
 * De [calculator] is als afhankelijkheid opgeslagen conform de UML-relatie
 * en wordt gebruikt door de algorithms bij hun interne matrixopbouw.
 *
 * @param algorithm   TSP-algoritme voor route-optimalisatie (standaard: TwoOpt + NearestNeighbor)
 * @param calculator  Afstandsberekening (standaard: Haversine)
 */
class RoutePlannerService(
    private var algorithm: TSPAlgorithm = TwoOptAlgorithm(),
    private val calculator: DistanceCalculator = HaversineCalculator()
) {

    /**
     * Optimaliseert een lijst van stops voor het gegeven criterium.
     * Conform UML: planRoute(List<Location>)
     *
     * @throws IllegalArgumentException bij minder dan 2 stops
     */
    fun planRoute(stops: List<Location>, criteria: OptimizationCriteria): Route {
        require(stops.size >= 2) { "Minimaal 2 stops vereist voor routeberekening." }

        val route = algorithm.solve(stops)
        return route.copy(criteria = criteria)
    }

    /**
     * Vervangt het actieve TSP-algoritme.
     * Conform UML: setAlgorithm(TSPAlgorithm)
     */
    fun setAlgorithm(newAlgorithm: TSPAlgorithm) {
        algorithm = newAlgorithm
    }
}
