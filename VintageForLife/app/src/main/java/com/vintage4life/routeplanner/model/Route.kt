package com.vintage4life.routeplanner.model

/**
 * A computed delivery route.
 * @param stops             Ordered list of locations to visit
 * @param totalDistanceKm   Total route distance in kilometres
 * @param estimatedTimeMin  Estimated travel time in minutes
 * @param criteria          Optimisation criterion used during calculation
 */
data class Route(
    val stops: List<Location>,
    val totalDistanceKm: Double = 0.0,
    val estimatedTimeMin: Double = 0.0,
    val criteria: OptimizationCriteria = OptimizationCriteria.DISTANCE
)
