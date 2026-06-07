package com.vintage4life.routeplanner.distance

import com.vintage4life.routeplanner.model.Location

/**
 * Implementeert [DistanceCalculator] met vooraf berekende waarden uit [RoadMatrix].
 *
 * Hierdoor kan de bestaande [DistanceMatrix]-constructor en de TSP-algoritmen
 * ongewijzigd gebruik maken van echte Mapbox-wegdata, zonder de interface te breken.
 *
 * @param locations  Geordende lijst van stops — zelfde volgorde als de matrix
 * @param values     N×N matrix met kosten (km, seconden, CO₂, enz.)
 */
class PrecomputedCalculator(
    locations: List<Location>,
    private val values: Array<DoubleArray>
) : DistanceCalculator {

    // O(1) index-opzoek via map
    private val indexMap: Map<String, Int> = locations
        .mapIndexed { i, loc -> loc.id to i }
        .toMap()

    override fun calculate(from: Location, to: Location): Double {
        val i = indexMap[from.id] ?: return 0.0
        val j = indexMap[to.id]  ?: return 0.0
        return values[i][j]
    }
}
