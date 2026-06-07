package com.vintage4life.routeplanner.model

/**
 * Een berekende bezorgroute.
 *
 * Conform UML: bevat [locations], [totalDistance], [addLocation()].
 * De [totalDistance] property voorziet in de [getTotalDistance()] methode.
 * Extra velden [estimatedTimeMin] en [criteria] zijn niet in UML maar zijn
 * noodzakelijk voor de bestaande UI-functionaliteit (reistijdweergave en
 * optimalisatiecriterium-selectie). Gedocumenteerd als toevoeging.
 *
 * @param locations         Geordende lijst van te bezoeken stops
 * @param totalDistance     Totale routeafstand in kilometer
 * @param estimatedTimeMin  Geschatte reistijd in minuten (toevoeging tov UML)
 * @param criteria          Gebruikte optimalisatiecriterium (toevoeging tov UML)
 */
data class Route(
    val locations: List<Location>,
    val totalDistance: Double = 0.0,
    val estimatedTimeMin: Double = 0.0,
    val totalCO2Grams: Double = 0.0,
    val criteria: OptimizationCriteria = OptimizationCriteria.DISTANCE
) {
    /**
     * Voegt een stop toe aan de route.
     * Conform UML: addLocation(Location).
     * Retourneert een nieuwe Route (Kotlin-idioom: immutable copy).
     */
    fun addLocation(location: Location): Route =
        copy(locations = locations + location)

}
// Kotlin genereert automatisch getTotalDistance() als JVM-getter van [totalDistance].
// Een expliciete methode zou een JVM-signatuurbotsing veroorzaken.
