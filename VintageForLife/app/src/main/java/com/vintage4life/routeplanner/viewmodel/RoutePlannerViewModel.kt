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

/**
 * ViewModel voor het RoutePlanner-scherm.
 * Conform UML: stops, route, error, addStop(), removeStop(), planRoute()
 */
class RoutePlannerViewModel(
    private val service: RoutePlannerService = RoutePlannerService()
) : ViewModel() {

    private val _stops = MutableStateFlow<List<Location>>(emptyList())
    val stops: StateFlow<List<Location>> = _stops.asStateFlow()

    private val _route = MutableStateFlow<Route?>(null)
    val route: StateFlow<Route?> = _route.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _routeGeometry = MutableStateFlow<List<DoubleArray>>(emptyList())
    val routeGeometry: StateFlow<List<DoubleArray>> = _routeGeometry.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var directionsClient: MapboxDirectionsClient? = null

    fun init(mapboxToken: String) {
        directionsClient = MapboxDirectionsClient(mapboxToken)
    }

    // ── Stop-beheer ───────────────────────────────────────────────────────────

    fun addStop(location: Location) {
        if (location.address.isBlank()) {
            _error.value = "Vul een adres in."
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

    // ── Route-oplossing ───────────────────────────────────────────────────────

    fun planRoute(criteria: OptimizationCriteria) {
        val currentStops = _stops.value
        if (currentStops.size < 2) {
            _error.value = "Voeg minimaal 2 stops toe om een route te berekenen."
            return
        }
        val client = directionsClient ?: run {
            _error.value = "Mapbox client niet geïnitialiseerd."
            return
        }

        _route.value         = null
        _routeGeometry.value = emptyList()
        _isLoading.value     = true

        viewModelScope.launch {
            try {
                val (solvedRoute, routeData) = withContext(Dispatchers.IO) {

                    // 1. Bouw N×N matrix met echte wegdata (per criterium andere volgorde)
                    val roadMatrix = client.buildRoadMatrix(currentStops)

                    // 2. TSP-oplossing op basis van echte wegdata of Haversine als fallback
                    val r = service.planRoute(currentStops, criteria, roadMatrix)

                    // 3. Haal geometrie op voor de GESLOTEN route (inclusief terugrit naar start)
                    //    Stuur locaties + eerste stop nog een keer zodat Mapbox de lus tekent
                    val closedStops = r.locations + r.locations.first()
                    val data = client.fetchRouteData(closedStops)

                    r to data
                }

                // Overschrijf metrics met Mapbox-data (meest accuraat voor gesloten route)
                val finalRoute = if (routeData != null) {
                    solvedRoute.copy(
                        totalDistance    = routeData.distanceMeters / 1000.0,
                        estimatedTimeMin = routeData.durationSeconds / 60.0
                        // totalCO2Grams blijft van de service (berekend per segment)
                    )
                } else {
                    solvedRoute
                }

                _route.value         = finalRoute
                _routeGeometry.value = routeData?.geometry ?: emptyList()
                // Toon geoptimaliseerde volgorde in de stoplijst
                _stops.value         = finalRoute.locations

            } catch (e: Exception) {
                _error.value = "Fout bij routeberekening: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ── Hulpmethoden ─────────────────────────────────────────────────────────

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
