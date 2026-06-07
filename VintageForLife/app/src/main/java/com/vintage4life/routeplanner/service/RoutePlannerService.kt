package com.vintage4life.routeplanner.service

import com.vintage4life.routeplanner.algorithm.TSPAlgorithm
import com.vintage4life.routeplanner.algorithm.TwoOptAlgorithm
import com.vintage4life.routeplanner.distance.CO2Calculator
import com.vintage4life.routeplanner.distance.DistanceMatrix
import com.vintage4life.routeplanner.distance.HaversineCalculator
import com.vintage4life.routeplanner.distance.PrecomputedCalculator
import com.vintage4life.routeplanner.distance.RoadMatrix
import com.vintage4life.routeplanner.distance.TimeCalculator
import com.vintage4life.routeplanner.model.Location
import com.vintage4life.routeplanner.model.OptimizationCriteria
import com.vintage4life.routeplanner.model.Route

/**
 * Kernservice die route-optimalisatie orkestreert.
 * Conform UML: [planRoute()], [setAlgorithm()]
 *
 * Wanneer een [RoadMatrix] beschikbaar is (Mapbox Matrix API), optimaliseert het
 * TSP-algoritme op échte wegdata:
 *  - DISTANCE  → minimaliseer wegafstand in km
 *  - TIME      → minimaliseer rijtijd in seconden
 *  - CO₂       → minimaliseer wegafstand × CO₂-factor
 *
 * Zonder RoadMatrix (fallback) wordt Haversine gebruikt — alle criteria geven
 * dan dezelfde route-volgorde.
 */
class RoutePlannerService(
    private var algorithm: TSPAlgorithm = TwoOptAlgorithm()
) {

    fun planRoute(
        stops: List<Location>,
        criteria: OptimizationCriteria,
        roadMatrix: RoadMatrix? = null
    ): Route {
        require(stops.size >= 2) { "Minimaal 2 stops vereist voor routeberekening." }

        // ── Kies de optimalisatiecalculator op basis van criteria en beschikbare data ──
        val optimizationCalculator = when {
            roadMatrix != null -> PrecomputedCalculator(
                stops,
                when (criteria) {
                    OptimizationCriteria.DISTANCE -> roadMatrix.distancesKm
                    OptimizationCriteria.TIME     -> roadMatrix.durationsSeconds
                    OptimizationCriteria.SUSTAINABILITY -> Array(stops.size) { i ->
                        DoubleArray(stops.size) { j ->
                            roadMatrix.distancesKm[i][j] * co2GramsPerKm(roadMatrix.distancesKm[i][j])
                        }
                    }
                }
            )
            else -> when (criteria) {
                OptimizationCriteria.DISTANCE       -> HaversineCalculator()
                OptimizationCriteria.TIME           -> TimeCalculator()
                OptimizationCriteria.SUSTAINABILITY -> CO2Calculator()
            }
        }

        val optimizedRoute = algorithm.solve(stops, optimizationCalculator)

        // ── Bereken display-metrics op werkelijke wegafstanden ──
        val haversine = HaversineCalculator()
        val timeCalc  = TimeCalculator()
        val co2Calc   = CO2Calculator()

        var totalDistKm   = 0.0
        var totalTimeMin  = 0.0
        var totalCO2Grams = 0.0

        for (i in 0 until optimizedRoute.locations.size - 1) {
            val from = optimizedRoute.locations[i]
            val to   = optimizedRoute.locations[i + 1]

            if (roadMatrix != null) {
                // Zoek indices terug via de originele stops-lijst
                val fi = stops.indexOfFirst { it.id == from.id }
                val ti = stops.indexOfFirst { it.id == to.id }
                if (fi >= 0 && ti >= 0) {
                    totalDistKm   += roadMatrix.distancesKm[fi][ti]
                    totalTimeMin  += roadMatrix.durationsSeconds[fi][ti] / 60.0
                    totalCO2Grams += roadMatrix.distancesKm[fi][ti] *
                                     co2GramsPerKm(roadMatrix.distancesKm[fi][ti])
                }
            } else {
                val dist = haversine.calculate(from, to)
                totalDistKm   += dist
                totalTimeMin  += timeCalc.calculate(from, to) * 60.0
                totalCO2Grams += co2Calc.calculate(from, to)
            }
        }

        return optimizedRoute.copy(
            totalDistance    = totalDistKm,
            estimatedTimeMin = totalTimeMin,
            totalCO2Grams    = totalCO2Grams,
            criteria         = criteria
        )
    }

    /** Conform UML: setAlgorithm(TSPAlgorithm) */
    fun setAlgorithm(newAlgorithm: TSPAlgorithm) {
        algorithm = newAlgorithm
    }

    /**
     * CO₂-emissiefactor in gram per km op basis van afstandsklasse.
     * Kortere ritten zijn minder efficiënt (koude motor, veel stoppen).
     */
    private fun co2GramsPerKm(distKm: Double): Double = when {
        distKm < 2.0 -> 220.0
        distKm < 8.0 -> 160.0
        else         -> 110.0
    }
}
