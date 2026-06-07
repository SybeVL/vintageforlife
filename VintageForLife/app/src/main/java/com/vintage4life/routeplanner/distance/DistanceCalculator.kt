package com.vintage4life.routeplanner.distance

import com.vintage4life.routeplanner.model.Location

/**
 * Strategy interface for computing distance between two locations.
 */
interface DistanceCalculator {
    /**  */
    fun calculate(from: Location, to: Location): Double
}
