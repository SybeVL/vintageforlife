package com.vintage4life.routeplanner.service

import com.vintage4life.routeplanner.algorithm.TSPAlgorithm
import com.vintage4life.routeplanner.algorithm.TwoOptAlgorithm
import com.vintage4life.routeplanner.distance.CO2Calculator
import com.vintage4life.routeplanner.distance.DistanceCalculator
import com.vintage4life.routeplanner.distance.HaversineCalculator
import com.vintage4life.routeplanner.distance.TimeCalculator
import com.vintage4life.routeplanner.model.Location
import com.vintage4life.routeplanner.model.OptimizationCriteria
import com.vintage4life.routeplanner.model.Route

/**
 * Kernservice die route-optimalisatie orkestreert.
 *
 * Conform UML:
 *  - velden: [algorithm: TSPAlgorithm]
 *  - methoden: [planRoute()], [setAlgorithm()]
 *
 * Het algoritme is uitwisselbaar via [setAlgorithm()] (Strategy pattern).
 * De calculator wordt dynamisch gekozen op basis van [OptimizationCriteria].
 *
 * @param algorithm   TSP-algoritme voor route-optimalisatie (standaard: TwoOpt + NearestNeighbor)
 */
class RoutePlannerService(
    private var algorithm: TSPAlgorithm = TwoOptAlgorithm()
) {

    /**
     * Optimaliseert een lijst van stops voor het gegeven criterium.
     * Conform UML: planRoute(List<Location>)
     *
     * @throws IllegalArgumentException bij minder dan 2 stops
     */
    fun planRoute(stops: List<Location>, criteria: OptimizationCriteria): Route {
        require(stops.size >= 2) { "Minimaal 2 stops vereist voor routeberekening." }

        val calculator = when (criteria) {
            OptimizationCriteria.DISTANCE -> HaversineCalculator()
            OptimizationCriteria.TIME     -> TimeCalculator()
            OptimizationCriteria.SUSTAINABILITY -> CO2Calculator()
        }

        // 1. Solve the TSP using the selected optimization cost strategy
        val optimizedRoute = algorithm.solve(stops, calculator)

        // 2. Calculate display metrics for the final result
        val distCalc = HaversineCalculator()
        val timeCalc = TimeCalculator()
        val co2Calc  = CO2Calculator()

        var totalDist = 0.0
        var totalTime = 0.0
        var totalCO2  = 0.0

        for (i in 0 until optimizedRoute.locations.size - 1) {
            val from = optimizedRoute.locations[i]
            val to   = optimizedRoute.locations[i+1]
            totalDist += distCalc.calculate(from, to)
            totalTime += timeCalc.calculate(from, to) * 60.0 // hours to minutes
            totalCO2  += co2Calc.calculate(from, to)
        }

        return optimizedRoute.copy(
            totalDistance    = totalDist,
            estimatedTimeMin = totalTime,
            totalCO2Grams    = totalCO2,
            criteria         = criteria
        )
    }

    /**
     * Vervangt het actieve TSP-algoritme.
     * Conform UML: setAlgorithm(TSPAlgorithm)
     */
    fun setAlgorithm(newAlgorithm: TSPAlgorithm) {
        algorithm = newAlgorithm
    }
}
