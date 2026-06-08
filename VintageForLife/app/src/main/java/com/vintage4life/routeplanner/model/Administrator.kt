package com.vintage4life.routeplanner.model

// Administrator user — can assign routes to drivers and create new routes.
data class Administrator(
    override val id: String,
    override val name: String,
    override val email: String
) : User(id, name, email, Role.ADMINISTRATOR) {

    // Assigns [route] to [chauffeur] and returns the new RouteAssignment.
    fun assignRoute(chauffeur: Chauffeur, route: Route): RouteAssignment =
        RouteAssignment(
            id        = "${id}-${chauffeur.id}-${System.currentTimeMillis()}",
            chauffeur = chauffeur,
            route     = route
        )

    // Creates a new empty route from a list of locations.
    fun createRoute(locations: List<Location>): Route =
        Route(locations = locations)
}
