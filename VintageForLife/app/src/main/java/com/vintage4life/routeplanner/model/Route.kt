package com.vintage4life.routeplanner.model

/**
 * A calculated delivery route with ordered stops and metrics.
 * [estimatedTimeMin] and [totalCO2Grams] are additions to the UML for UI display.
 *  @param locations         Ordered list of stops to visit
 *  @param totalDistance     Total route distance in kilometers
 *  @param estimatedTimeMin  Estimated travel time in minutes (addition compared to UML)
 *  @param criteria          Used optimization criteria (addition compared to UML)
 *  @param criteria          Used optimization criteria (addition compared to UML)
 */
data class Route(
    val locations: List<Location>,
    val totalDistance: Double = 0.0,
    val estimatedTimeMin: Double = 0.0,
    val totalCO2Grams: Double = 0.0,
    val criteria: OptimizationCriteria = OptimizationCriteria.DISTANCE
) {
    // Returns a new Route with [location] appended. Kotlin immutable copy pattern.
    fun addLocation(location: Location): Route =
        copy(locations = locations + location)
}
// Kotlin auto-generates getTotalDistance() as a JVM getter — no explicit method needed.
