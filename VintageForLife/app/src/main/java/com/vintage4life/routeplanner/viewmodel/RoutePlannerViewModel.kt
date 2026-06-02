package com.vintage4life.routeplanner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
 * ViewModel for the Routeplanner screen.
 * Manages UI state and delegates business logic to [RoutePlannerService].
 *
 * Exposes:
 *  - [stopList]   — live list of stops added by the user
 *  - [routeResult] — computed optimised route (null until solved)
 *  - [isLoading]  — true while route calculation is running
 *  - [errorMessage] — validation or calculation errors
 */
class RoutePlannerViewModel(
    private val service: RoutePlannerService = RoutePlannerService()
) : ViewModel() {

    private val _stopList = MutableStateFlow<List<Location>>(emptyList())
    val stopList: StateFlow<List<Location>> = _stopList.asStateFlow()

    private val _routeResult = MutableStateFlow<Route?>(null)
    val routeResult: StateFlow<Route?> = _routeResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ── Stop management ───────────────────────────────────────────────────────

    /**
     * Add a stop to the list.
     * In a production app, address geocoding (Mapbox Geocoding API) would run here.
     * For the PoC, lat/lon are passed directly.
     */
    fun addStop(address: String, name: String = "", lat: Double, lon: Double) {
        if (address.isBlank()) {
            _errorMessage.value = "Vul een adres in."
            return
        }
        val stop = Location(
            id = UUID.randomUUID().toString(),
            name = name,
            address = address,
            latitude = lat,
            longitude = lon
        )
        _stopList.value = _stopList.value + stop
        _errorMessage.value = null
    }

    fun removeStop(location: Location) {
        _stopList.value = _stopList.value.filter { it.id != location.id }
    }

    fun moveStop(from: Int, to: Int) {
        val list = _stopList.value.toMutableList()
        if (from < 0 || to < 0 || from >= list.size || to >= list.size) return
        val item = list.removeAt(from)
        list.add(to, item)
        _stopList.value = list
    }

    // ── Route solving ─────────────────────────────────────────────────────────

    fun solveRoute(criteria: OptimizationCriteria) {
        val stops = _stopList.value
        if (stops.size < 2) {
            _errorMessage.value = "Voeg minimaal 2 stops toe om een route te berekenen."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = withContext(Dispatchers.Default) {
                    service.optimizeRoute(stops, criteria)
                }
                _routeResult.value = result
                // Update stop list to reflect optimised order
                _stopList.value = result.stops
            } catch (e: Exception) {
                _errorMessage.value = "Fout bij routeberekening: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    fun clearAll() {
        _stopList.value = emptyList()
        _routeResult.value = null
        _errorMessage.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
