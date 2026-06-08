package com.vintage4life.routeplanner.algorithm

import com.vintage4life.routeplanner.distance.DistanceCalculator
import com.vintage4life.routeplanner.model.Location
import com.vintage4life.routeplanner.model.Route

/** Strategy interface for TSP algorithms. Implementations define the optimisation strategy. */
interface TSPAlgorithm {
    /** Returns an optimised route through all [locations] based on the given [calculator]. */
    fun solve(locations: List<Location>, calculator: DistanceCalculator): Route
}
