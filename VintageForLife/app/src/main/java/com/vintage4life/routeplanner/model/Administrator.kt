package com.vintage4life.routeplanner.model

/**
 * Beheerder-gebruiker.
 * Conform UML: heeft [assignRoute()] en [createRoute()] methoden.
 */
data class Administrator(
    override val id: String,
    override val name: String,
    override val email: String
) : User(id, name, email, Role.ADMINISTRATOR) {

    /**
     * Wijst een route toe aan een chauffeur.
     * Conform UML: assignRoute(Chauffeur, Route): void
     * Retourneert de nieuwe [RouteAssignment] (Kotlin-idioom: geen side-effects).
     */
    fun assignRoute(chauffeur: Chauffeur, route: Route): RouteAssignment =
        RouteAssignment(
            id        = "${id}-${chauffeur.id}-${System.currentTimeMillis()}",
            chauffeur = chauffeur,
            route     = route
        )

    /**
     * Maakt een nieuwe lege route aan vanuit een lijst van locaties.
     * Conform UML: createRoute(List<Location>): Route
     */
    fun createRoute(locations: List<Location>): Route =
        Route(locations = locations)
}
