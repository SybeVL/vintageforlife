package com.vintage4life.routeplanner.distance

import com.vintage4life.routeplanner.model.Location

// interface for calculating distance between location a and b
interface DistanceCalculator {
    /**  */
    fun calculate(from: Location, to: Location): Double
}
