package com.vintage4life.routeplanner.repository

import com.vintage4life.routeplanner.model.Location
import com.vintage4life.routeplanner.model.Route

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
