package com.vintage4life.routeplanner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vintage4life.routeplanner.distance.MapboxDirectionsClient
import com.vintage4life.routeplanner.model.Location
import com.vintage4life.routeplanner.model.OptimizationCriteria
import com.vintage4life.routeplanner.model.Route
import com.vintage4life.routeplanner.service.RoutePlannerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ViewModel for the route planner screen — exposes stops, route, and loading state.
class RoutePlannerViewModel(
    private val service: RoutePlannerService = RoutePlannerService()
) : ViewModel() {

    private val _stops         = MutableStateFlow<List<Location>>(emptyList())
    val stops: StateFlow<List<Location>> = _stops.asStateFlow()

    private val _route         = MutableStateFlow<Route?>(null)
    val route: StateFlow<Route?> = _route.asStateFlow()

    private val _error         = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _routeGeometry = MutableStateFlow<List<DoubleArray>>(emptyList())
    val routeGeometry: StateFlow<List<DoubleArray>> = _routeGeometry.asStateFlow()

    private val _isLoading     = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var directionsClient: MapboxDirectionsClient? = null

    // Must be called once after the Mapbox token is available.
    fun init(mapboxToken: String) {
        directionsClient = MapboxDirectionsClient(mapboxToken)
    }

    fun addStop(location: Location) {
        if (location.address.isBlank()) {
            _error.value = "Please enter an address."
            return
        }
        _stops.value         = _stops.value + location
        _route.value         = null
        _routeGeometry.value = emptyList()
        _error.value         = null
    }

    fun removeStop(location: Location) {
        _stops.value         = _stops.value.filter { it.id != location.id }
        _route.value         = null
        _routeGeometry.value = emptyList()
    }

    fun removeStopAt(index: Int) {
        _stops.value         = _stops.value.toMutableList().also { it.removeAt(index) }
        _route.value         = null
        _routeGeometry.value = emptyList()
    }

    fun moveStop(from: Int, to: Int) {
        val list = _stops.value.toMutableList()
        if (from < 0 || to < 0 || from >= list.size || to >= list.size) return
        list.add(to, list.removeAt(from))
        _stops.value = list
    }

    fun planRoute(criteria: OptimizationCriteria) {
        val currentStops = _stops.value
        if (currentStops.size < 2) {
            _error.value = "Add at least 2 stops to calculate a route."
            return
        }
        val client = directionsClient ?: run {
            _error.value = "Mapbox client not initialised."
            return
        }

        _route.value         = null
        _routeGeometry.value = emptyList()
        _isLoading.value     = true

        viewModelScope.launch {
            try {
                val (solvedRoute, routeData) = withContext(Dispatchers.IO) {

                    // 1. Build N×N road matrix from real Directions API data
                    val roadMatrix = client.buildRoadMatrix(currentStops)

                    // 2. Solve TSP on road data; falls back to Haversine if API failed
                    val r = service.planRoute(currentStops, criteria, roadMatrix)

                    // 3. Fetch geometry for the closed route (stops + return to first)
                    val closedStops = r.locations + r.locations.first()
                    val data = client.fetchRouteData(closedStops)

                    r to data
                }

                // Override distance/time with Mapbox totals — most accurate for the full loop
                val finalRoute = if (routeData != null) {
                    solvedRoute.copy(
                        totalDistance    = routeData.distanceMeters / 1000.0,
                        estimatedTimeMin = routeData.durationSeconds / 60.0
                        // totalCO2Grams stays from the service (computed per segment)
                    )
                } else {
                    solvedRoute
                }

                _route.value         = finalRoute
                _routeGeometry.value = routeData?.geometry ?: emptyList()
                _stops.value         = finalRoute.locations  // reflect optimised order in UI

            } catch (e: Exception) {
                _error.value = "Route calculation failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearAll() {
        _stops.value         = emptyList()
        _route.value         = null
        _routeGeometry.value = emptyList()
        _error.value         = null
    }

    fun clearError() {
        _error.value = null
    }
}
