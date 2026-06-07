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
import java.util.UUID

/**
 * ViewModel for the RoutePlanner screen.
 * Manages UI state and delegates business logic to [RoutePlannerService]
 * and road geometry fetching to [MapboxDirectionsClient].
 *
 * Exposes:
 *  - [stopList]       — live list of stops added by the user
 *  - [routeResult]    — computed optimised route (null until solved)
 *  - [routeGeometry]  — real road coordinates from Mapbox Directions API
 *  - [isLoading]      — true while route calculation is in progress
 *  - [errorMessage]   — validation or calculation errors (one-shot)
 */
class RoutePlannerViewModel(
    private val service: RoutePlannerService = RoutePlannerService()
) : ViewModel() {

    private val _stopList = MutableStateFlow<List<Location>>(emptyList())
    val stopList: StateFlow<List<Location>> = _stopList.asStateFlow()

    private val _routeResult = MutableStateFlow<Route?>(null)
    val routeResult: StateFlow<Route?> = _routeResult.asStateFlow()

    /** Real road geometry: ordered list of [lon, lat] pairs from Mapbox Directions. */
    private val _routeGeometry = MutableStateFlow<List<DoubleArray>>(emptyList())
    val routeGeometry: StateFlow<List<DoubleArray>> = _routeGeometry.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var directionsClient: MapboxDirectionsClient? = null

    // ── Initialisation ────────────────────────────────────────────────────────

    /**
     * Must be called once from the Activity after location permission is granted.
     * Initialises the Mapbox Directions client with the access token.
     */
    fun init(mapboxToken: String) {
        directionsClient = MapboxDirectionsClient(mapboxToken)
    }

    // ── Stop management ───────────────────────────────────────────────────────

    /**
     * Adds a geocoded stop to the list.
     * Clears any active solved route so the map resets to pins-only mode.
     */
    fun addStop(name: String, address: String, lat: Double, lon: Double) {
        if (address.isBlank()) {
            _errorMessage.value = "Vul een adres in."
            return
        }
        val stop = Location(
            id        = UUID.randomUUID().toString(),
            name      = name,
            address   = address,
            latitude  = lat,
            longitude = lon
        )
        _stopList.value  = _stopList.value + stop
        _routeResult.value   = null
        _routeGeometry.value = emptyList()
        _errorMessage.value  = null
    }

    /** Removes a stop by its Location object and clears the active route. */
    fun removeStop(location: Location) {
        _stopList.value      = _stopList.value.filter { it.id != location.id }
        _routeResult.value   = null
        _routeGeometry.value = emptyList()
    }

    /** Removes a stop by index and clears the active route. */
    fun removeStopAt(index: Int) {
        _stopList.value      = _stopList.value.toMutableList().also { it.removeAt(index) }
        _routeResult.value   = null
        _routeGeometry.value = emptyList()
    }

    /** Reorders stops via drag-and-drop. */
    fun moveStop(from: Int, to: Int) {
        val list = _stopList.value.toMutableList()
        if (from < 0 || to < 0 || from >= list.size || to >= list.size) return
        val item = list.removeAt(from)
        list.add(to, item)
        _stopList.value = list
    }

    // ── Route solving ─────────────────────────────────────────────────────────

    /**
     * Solves the TSP on a background thread, then fetches real road geometry
     * from Mapbox Directions. Updates [routeResult] and [routeGeometry] when done.
     */
    fun solveRoute(criteria: OptimizationCriteria) {
        val stops = _stopList.value
        if (stops.size < 2) {
            _errorMessage.value = "Voeg minimaal 2 stops toe om een route te berekenen."
            return
        }
        val client = directionsClient ?: run {
            _errorMessage.value = "Mapbox client niet geïnitialiseerd — roep init() aan."
            return
        }

        _routeResult.value   = null
        _routeGeometry.value = emptyList()
        _isLoading.value     = true

        viewModelScope.launch {
            try {
                val (solvedRoute, geometry) = withContext(Dispatchers.IO) {
                    val route    = service.optimizeRoute(stops, criteria)
                    val geometry = client.fetchRouteGeometry(route.stops)
                    route to geometry
                }
                _routeResult.value   = solvedRoute
                _routeGeometry.value = geometry
                // Update stop list to reflect optimised order
                _stopList.value      = solvedRoute.stops
            } catch (e: Exception) {
                _errorMessage.value = "Fout bij routeberekening: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    /** Resets all stops, route, geometry and error state. */
    fun clearAll() {
        _stopList.value      = emptyList()
        _routeResult.value   = null
        _routeGeometry.value = emptyList()
        _errorMessage.value  = null
    }

    /** Clears the one-shot error message after it has been displayed. */
    fun clearError() {
        _errorMessage.value = null
    }
}
