package com.vintage4life.routeplanner.ui

import android.location.Geocoder
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.vintage4life.routeplanner.model.OptimizationCriteria
import com.vintage4life.routeplanner.viewmodel.RoutePlannerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Main screen of the RoutePlanner app.
 *
 * Responsibilities:
 *  - Full-screen Mapbox map with live GPS puck
 *  - Tap on map to auto-fill the address field
 *  - Live address label on the user's current location
 *  - Bottom panel: stop input, optimisation mode selector, action buttons, stop list
 *  - Redraws pins/route whenever state changes via [MapAnnotations]
 */
@Composable
fun RoutePlannerScreen(viewModel: RoutePlannerViewModel) {
    val context = LocalContext.current

    val stopList      by viewModel.stopList.collectAsState()
    val routeResult   by viewModel.routeResult.collectAsState()
    val routeGeometry by viewModel.routeGeometry.collectAsState()
    val errorMessage  by viewModel.errorMessage.collectAsState()

    val viewportState = rememberMapViewportState()
    val scope         = rememberCoroutineScope()
    val geocoder      = remember { Geocoder(context, Locale.getDefault()) }

    var nameInput    by remember { mutableStateOf("") }
    var addressInput by remember { mutableStateOf("") }
    var isSearching  by remember { mutableStateOf(false) }
    var selectedCriteria by remember { mutableStateOf(OptimizationCriteria.DISTANCE) }

    // References held stable across recompositions
    var mapViewRef               by remember { mutableStateOf<com.mapbox.maps.MapView?>(null) }
    var stopsAnnotationManager   by remember { mutableStateOf<PointAnnotationManager?>(null) }
    var addressAnnotationManager by remember { mutableStateOf<PointAnnotationManager?>(null) }
    var positionListener         by remember { mutableStateOf<OnIndicatorPositionChangedListener?>(null) }

    // Redraw pins/route whenever stops, the solved route, or geometry changes
    LaunchedEffect(routeResult, routeGeometry, stopList) {
        val mv = mapViewRef ?: return@LaunchedEffect
        val am = stopsAnnotationManager ?: return@LaunchedEffect
        if (routeResult != null) {
            MapAnnotations.drawRoute(mv, routeResult!!.stops, routeGeometry, am)
        } else {
            MapAnnotations.clearRoute(mv, am)
            MapAnnotations.showPins(stopList, am)
        }
    }

    // Clean up position listener when composable leaves composition
    DisposableEffect(Unit) {
        onDispose {
            positionListener?.let {
                mapViewRef?.location?.removeOnIndicatorPositionChangedListener(it)
            }
        }
    }

    Column(Modifier.fillMaxSize()) {

        // ── Map ──────────────────────────────────────────────────────────────
        Box(Modifier.weight(1f)) {
            MapboxMap(
                modifier = Modifier.fillMaxSize(),
                mapViewportState = viewportState,
                onMapClickListener = { point ->
                    // Tap on map → reverse-geocode and fill the address field
                    scope.launch {
                        val address = withContext(Dispatchers.IO) {
                            try {
                                geocoder.getFromLocation(point.latitude(), point.longitude(), 1)
                                    ?.firstOrNull()?.getAddressLine(0)
                            } catch (e: Exception) { null }
                        }
                        if (address != null) addressInput = address
                    }
                    true
                }
            ) {
                MapEffect(Unit) { mapView ->
                    mapViewRef = mapView

                    mapView.mapboxMap.loadStyle(Style.MAPBOX_STREETS)

                    mapView.location.updateSettings {
                        enabled             = true
                        locationPuck        = createDefault2DPuck(withBearing = true)
                        puckBearing         = PuckBearing.COURSE
                        puckBearingEnabled  = true
                    }

                    // Two separate annotation managers:
                    // one for persistent stop pins, one for the live address label
                    stopsAnnotationManager   = mapView.annotations.createPointAnnotationManager()
                    addressAnnotationManager = mapView.annotations.createPointAnnotationManager()

                    var hasInitiallyCentered = false

                    val newListener = OnIndicatorPositionChangedListener { point ->
                        // Centre the camera on the user's first GPS fix
                        if (!hasInitiallyCentered) {
                            viewportState.transitionToFollowPuckState()
                            hasInitiallyCentered = true
                        }

                        // Only show live address label when no route is active
                        if (routeResult == null) {
                            scope.launch {
                                val address = withContext(Dispatchers.IO) {
                                    try {
                                        geocoder.getFromLocation(
                                            point.latitude(), point.longitude(), 1
                                        )?.firstOrNull()?.getAddressLine(0)
                                    } catch (e: Exception) { null }
                                }
                                if (address != null) {
                                    addressAnnotationManager?.deleteAll()
                                    addressAnnotationManager?.create(
                                        PointAnnotationOptions()
                                            .withPoint(
                                                Point.fromLngLat(point.longitude(), point.latitude())
                                            )
                                            .withTextField(address)
                                    )
                                }
                            }
                        } else {
                            addressAnnotationManager?.deleteAll()
                        }
                    }
                    positionListener = newListener
                    mapView.location.addOnIndicatorPositionChangedListener(newListener)
                }
            }
        }

        // ── Bottom panel ─────────────────────────────────────────────────────
        Surface(
            tonalElevation = 4.dp,
            modifier = Modifier.navigationBarsPadding()
        ) {
            Column(
                Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                // Error message
                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                    LaunchedEffect(it) { viewModel.clearError() }
                }

                // Input fields
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Stop name (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = addressInput,
                    onValueChange = { addressInput = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("e.g. Dam 1, Amsterdam") }
                )

                // Add stop button — geocodes the address on a background thread
                Button(
                    onClick = {
                        if (addressInput.isNotBlank()) {
                            isSearching = true
                            scope.launch {
                                val location = withContext(Dispatchers.IO) {
                                    try {
                                        geocoder.getFromLocationName(addressInput, 1)?.firstOrNull()
                                    } catch (e: Exception) { null }
                                }
                                if (location != null) {
                                    val name = if (nameInput.isNotBlank()) nameInput else addressInput
                                    viewModel.addStop(
                                        name    = name,
                                        address = addressInput,
                                        lat     = location.latitude,
                                        lon     = location.longitude
                                    )
                                    addressInput = ""
                                    nameInput    = ""
                                } else {
                                    viewModel.clearError()
                                }
                                isSearching = false
                            }
                        }
                    },
                    enabled = !isSearching && addressInput.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isSearching) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Add stop")
                    }
                }

                // Stop list
                if (stopList.isNotEmpty()) {
                    LazyColumn(Modifier.heightIn(max = 110.dp)) {
                        itemsIndexed(stopList) { index, stop ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${index + 1}. ${stop.name.ifBlank { stop.address }}",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = { viewModel.removeStopAt(index) }) {
                                    Text("Remove")
                                }
                            }
                        }
                    }
                }

                // Optimisation mode selector
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Optimise:",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    OptimizationCriteria.entries.forEach { criteria ->
                        FilterChip(
                            selected = selectedCriteria == criteria,
                            onClick  = { selectedCriteria = criteria },
                            label = {
                                Text(
                                    when (criteria) {
                                        OptimizationCriteria.DISTANCE       -> "Distance"
                                        OptimizationCriteria.TIME           -> "Time"
                                        OptimizationCriteria.SUSTAINABILITY -> "CO₂"
                                    },
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )
                    }
                }

                // Action buttons
                val isLoading by viewModel.isLoading.collectAsState()
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick  = { viewModel.solveRoute(selectedCriteria) },
                        enabled  = stopList.size >= 2 && !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color       = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Solve route")
                        }
                    }

                    OutlinedButton(
                        onClick  = { viewModel.clearAll() },
                        modifier = Modifier.weight(1f)
                    ) { Text("Clear") }
                }

                // Route summary
                routeResult?.let {
                    Text(
                        text  = "Route: %.1f km  |  ~%.0f min".format(it.totalDistanceKm, it.estimatedTimeMin),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
