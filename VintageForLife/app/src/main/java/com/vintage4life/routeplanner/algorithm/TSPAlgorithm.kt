package com.vintage4life.routeplanner.algorithm

import com.vintage4life.routeplanner.model.Location
import com.vintage4life.routeplanner.model.OptimizationCriteria
import com.vintage4life.routeplanner.model.Route

/**
 * Strategy interface voor TSP-oplossingsalgoritmen.
 * Conform UML: solve(List<Location>): Route
 *
 * UML-verschil tov vorige implementatie: eerder was de signatuur
 * solve(DistanceMatrix): List<Int>. Nu werkt de interface op domeinniveau
 * (Location → Route) zodat de [DistanceMatrix] een intern implementatiedetail
 * blijft en niet lekt naar de servicelaag.
 */
interface TSPAlgorithm {
    /**
     * Berekent een geoptimaliseerde route langs alle gegeven stops.
     *
     * @param locations  Lijst van te bezoeken stops (minimaal 2)
     * @param criteria   Optimalisatiecriterium (Distance, Time, etc.)
     * @return           Geoptimaliseerde route met geordende stops en afstand
     */
    fun solve(locations: List<Location>, criteria: OptimizationCriteria): Route
}
