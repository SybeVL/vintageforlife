package com.vintage4life.routeplanner.repository

import com.vintage4life.routeplanner.model.Location
import com.vintage4life.routeplanner.model.Route

/**
 * Repository for persisting and retrieving routes.
 * Currently in-memory; replace with Room database for production.
 */
class RouteRepository {

    private val savedRoutes = mutableListOf<Route>()

    fun saveRoute(route: Route) {
        savedRoutes.add(route)
    }

    fun getAllRoutes(): List<Route> = savedRoutes.toList()

    fun clearRoutes() {
        savedRoutes.clear()
    }
}
