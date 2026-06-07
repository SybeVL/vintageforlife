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
 *
 * Conform UML:
 *  - velden: [stops], [route], [error]
 *  - methoden: [addStop(location)], [removeStop(location)], [planRoute()]
 *
 * Toevoegingen tov UML (noodzakelijk voor bestaande functionaliteit):
 *  - [routeGeometry]  — wegcoördinaten voor kaartvisualisatie
 *  - [isLoading]      — loading-indicator tijdens berekening
 *  - [init(token)]    — Mapbox-token initialisatie
 *  - [clearAll()]     — alles wissen inclusief kaart
 *  - [clearError()]   — éénmalige foutmelding wissen
 *  - [removeStopAt()] — verwijderen via index (voor stoplijst-UI)
 *  - [moveStop()]     — herordenen via drag-and-drop
 */
class RoutePlannerViewModel(
    private val service: RoutePlannerService = RoutePlannerService()
) : ViewModel() {

    // ── State — conform UML-veldnamen ─────────────────────────────────────────

    private val _stops = MutableStateFlow<List<Location>>(emptyList())
    /** Conform UML: stops: List<Location> */
    val stops: StateFlow<List<Location>> = _stops.asStateFlow()

    private val _route = MutableStateFlow<Route?>(null)
    /** Conform UML: route: Route */
    val route: StateFlow<Route?> = _route.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    /** Conform UML: error: String */
    val error: StateFlow<String?> = _error.asStateFlow()

    /** Wegcoördinaten van Mapbox Directions API. Toevoeging tov UML. */
    private val _routeGeometry = MutableStateFlow<List<DoubleArray>>(emptyList())
    val routeGeometry: StateFlow<List<DoubleArray>> = _routeGeometry.asStateFlow()

    /** Loading-indicator. Toevoeging tov UML. */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var directionsClient: MapboxDirectionsClient? = null

    // ── Initialisatie ─────────────────────────────────────────────────────────

    /**
     * Initialiseert de Mapbox Directions client met het toegangstoken.
     * Moet eenmalig vanuit de Activity worden aangeroepen na toekenning van locatiepermissies.
     * Toevoeging tov UML.
     */
    fun init(mapboxToken: String) {
        directionsClient = MapboxDirectionsClient(mapboxToken)
    }

    // ── Stop-beheer — conform UML ─────────────────────────────────────────────

    /**
     * Voegt een stop toe aan de lijst.
     * Conform UML: addStop(location): void
     */
    fun addStop(location: Location) {
        if (location.address.isBlank()) {
            _error.value = "Vul een adres in."
            return
        }
        _stops.value        = _stops.value + location
        _route.value        = null
        _routeGeometry.value = emptyList()
        _error.value        = null
    }

    /**
     * Verwijdert een stop op basis van het Location-object.
     * Conform UML: removeStop(location): void
     */
    fun removeStop(location: Location) {
        _stops.value        = _stops.value.filter { it.id != location.id }
        _route.value        = null
        _routeGeometry.value = emptyList()
    }

    /** Verwijdert een stop op basis van index. Toevoeging tov UML. */
    fun removeStopAt(index: Int) {
        _stops.value        = _stops.value.toMutableList().also { it.removeAt(index) }
        _route.value        = null
        _routeGeometry.value = emptyList()
    }

    /** Herordent stops via drag-and-drop. Toevoeging tov UML. */
    fun moveStop(from: Int, to: Int) {
        val list = _stops.value.toMutableList()
        if (from < 0 || to < 0 || from >= list.size || to >= list.size) return
        list.add(to, list.removeAt(from))
        _stops.value = list
    }

    // ── Route-oplossing — conform UML ─────────────────────────────────────────

    /**
     * Optimaliseert de huidige stoplijst en haalt weggeometrie op.
     * Conform UML: planRoute(List<Location>)
     *
     * UML-afwijking: accepteert ook een [criteria] parameter zodat de gebruiker
     * het optimalisatiecriterium kan kiezen. Geen functionele wijziging.
     */
    fun planRoute(criteria: OptimizationCriteria) {
        val currentStops = _stops.value
        if (currentStops.size < 2) {
            _error.value = "Voeg minimaal 2 stops toe om een route te berekenen."
            return
        }
        val client = directionsClient ?: run {
            _error.value = "Mapbox client niet geïnitialiseerd — roep init() aan."
            return
        }

        _route.value        = null
        _routeGeometry.value = emptyList()
        _isLoading.value    = true

        viewModelScope.launch {
            try {
                val (solvedRoute, geometry) = withContext(Dispatchers.IO) {
                    val r = service.planRoute(currentStops, criteria)
                    val g = client.fetchRouteGeometry(r.locations)
                    r to g
                }
                _route.value        = solvedRoute
                _routeGeometry.value = geometry
                _stops.value        = solvedRoute.locations
            } catch (e: Exception) {
                _error.value = "Fout bij routeberekening: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ── Hulpmethoden — toevoegingen tov UML ──────────────────────────────────

    /** Reset alle stops, route, geometrie en foutmelding. */
    fun clearAll() {
        _stops.value        = emptyList()
        _route.value        = null
        _routeGeometry.value = emptyList()
        _error.value        = null
    }

    /** Wist de éénmalige foutmelding na weergave. */
    fun clearError() {
        _error.value = null
    }
}
