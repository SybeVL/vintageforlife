package com.vintage4life.routeplanner.distance

import com.vintage4life.routeplanner.model.Location

/**
 * Strategy interface for computing distance or cost between two locations.
 */
interface DistanceCalculator {
    /** Returns distance or weighted cost between [from] and [to]. */
    fun calculate(from: Location, to: Location): Double
}
