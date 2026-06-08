package com.vintage4life.routeplanner.distance

import com.vintage4life.routeplanner.model.Location

// implements DistanceCalculator with RoadMatrix values. DistanceCalculator-interface does not have to be broken like this.
class PrecomputedCalculator(
    locations: List<Location>,
    private val values: Array<DoubleArray>
) : DistanceCalculator {

    private val indexMap: Map<String, Int> = locations
        .mapIndexed { i, loc -> loc.id to i }
        .toMap()

    override fun calculate(from: Location, to: Location): Double {
        val i = indexMap[from.id] ?: return 0.0
        val j = indexMap[to.id]  ?: return 0.0
        return values[i][j]
    }
}
