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
import com.vintage4life.routeplanner.model.Location
import com.vintage4life.routeplanner.model.OptimizationCriteria
import com.vintage4life.routeplanner.viewmodel.RoutePlannerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID

@Composable
fun RoutePlannerScreen(viewModel: RoutePlannerViewModel) {
    val context = LocalContext.current

    val stops         by viewModel.stops.collectAsState()
    val optimisedStops by viewModel.optimisedStops.collectAsState()
    val route         by viewModel.route.collectAsState()
    val routeGeometry by viewModel.routeGeometry.collectAsState()
    val error         by viewModel.error.collectAsState()

    // Show the optimised order after calculation, original order while editing
    val displayStops = if (route != null && optimisedStops.isNotEmpty()) optimisedStops else stops

    // Default camera position: the Netherlands
    val viewportState = rememberMapViewportState {
        setCameraOptions {
            center(Point.fromLngLat(5.3878, 52.1561))
            zoom(7.0)
        }
    }

    val scope    = rememberCoroutineScope()
    val geocoder = remember { Geocoder(context, Locale.getDefault()) }

    var nameInput        by remember { mutableStateOf("") }
    var addressInput     by remember { mutableStateOf("") }
    var isSearching      by remember { mutableStateOf(false) }
    var selectedCriteria by remember { mutableStateOf(OptimizationCriteria.DISTANCE) }

    var mapViewRef               by remember { mutableStateOf<com.mapbox.maps.MapView?>(null) }
    var stopsAnnotationManager   by remember { mutableStateOf<PointAnnotationManager?>(null) }
    var addressAnnotationManager by remember { mutableStateOf<PointAnnotationManager?>(null) }
    var positionListener         by remember { mutableStateOf<OnIndicatorPositionChangedListener?>(null) }

    // Redraw pins/route whenever route, geometry or stops change
    LaunchedEffect(route, routeGeometry, displayStops) {
        val mv = mapViewRef ?: return@LaunchedEffect
        val am = stopsAnnotationManager ?: return@LaunchedEffect
        if (route != null) {
            MapAnnotations.drawRoute(mv, route!!.locations, routeGeometry, am)
        } else {
            MapAnnotations.clearRoute(mv, am)
            MapAnnotations.showPins(displayStops, am)
        }
    }

    // Clean up the position listener when the composable leaves the composition
    DisposableEffect(Unit) {
        onDispose {
            positionListener?.let {
                mapViewRef?.location?.removeOnIndicatorPositionChangedListener(it)
            }
        }
    }

    Column(Modifier.fillMaxSize()) {

        // Map
        Box(Modifier.weight(1f)) {
            MapboxMap(
                modifier         = Modifier.fillMaxSize(),
                mapViewportState = viewportState,
                // Tapping the map reverse-geocodes the point into the address field
                onMapClickListener = { point ->
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
                        enabled            = true
                        locationPuck       = createDefault2DPuck(withBearing = true)
                        puckBearing        = PuckBearing.COURSE
                        puckBearingEnabled = true
                    }

                    stopsAnnotationManager   = mapView.annotations.createPointAnnotationManager()
                    addressAnnotationManager = mapView.annotations.createPointAnnotationManager()

                    // Show the current address label while no route is active
                    val newListener = OnIndicatorPositionChangedListener { point ->
                        if (route == null) {
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
                                            .withPoint(Point.fromLngLat(point.longitude(), point.latitude()))
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

        // Bottom panel
        Surface(
            tonalElevation = 4.dp,
            modifier       = Modifier.navigationBarsPadding()
        ) {
            Column(
                Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                error?.let {
                    Text(
                        text  = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                    LaunchedEffect(it) { viewModel.clearError() }
                }

                OutlinedTextField(
                    value         = nameInput,
                    onValueChange = { nameInput = it },
                    label         = { Text("Stopnaam (optioneel)") },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true
                )
                OutlinedTextField(
                    value         = addressInput,
                    onValueChange = { addressInput = it },
                    label         = { Text("Adres") },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    placeholder   = { Text("bijv. Dam 1, Amsterdam") }
                )

                Button(
                    onClick = {
                        if (addressInput.isNotBlank()) {
                            isSearching = true
                            scope.launch {
                                val geocoded = withContext(Dispatchers.IO) {
                                    try {
                                        geocoder.getFromLocationName(addressInput, 1)?.firstOrNull()
                                    } catch (e: Exception) { null }
                                }
                                if (geocoded != null) {
                                    val name = if (nameInput.isNotBlank()) nameInput else addressInput
                                    viewModel.addStop(
                                        Location(
                                            id        = UUID.randomUUID().toString(),
                                            name      = name,
                                            address   = addressInput,
                                            latitude  = geocoded.latitude,
                                            longitude = geocoded.longitude
                                        )
                                    )
                                    addressInput = ""; nameInput = ""
                                }
                                isSearching = false
                            }
                        }
                    },
                    enabled  = !isSearching && addressInput.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isSearching)
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    else
                        Text("Stop toevoegen")
                }

                if (displayStops.isNotEmpty()) {
                    LazyColumn(Modifier.heightIn(max = 110.dp)) {
                        itemsIndexed(displayStops) { index, stop ->
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${index + 1}. ${stop.name.ifBlank { stop.address }}",
                                    style    = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = { viewModel.removeStop(stop) }) {
                                    Text("Verwijder")
                                }
                            }
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text("Optimaliseer:", style = MaterialTheme.typography.bodySmall)
                    OptimizationCriteria.entries.forEach { criteria ->
                        FilterChip(
                            selected = selectedCriteria == criteria,
                            onClick  = { selectedCriteria = criteria },
                            label    = {
                                Text(
                                    when (criteria) {
                                        OptimizationCriteria.DISTANCE       -> "Afstand"
                                        OptimizationCriteria.TIME           -> "Tijd"
                                        OptimizationCriteria.SUSTAINABILITY -> "CO₂"
                                    },
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )
                    }
                }

                val isLoading by viewModel.isLoading.collectAsState()
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick  = { viewModel.planRoute(selectedCriteria) },
                        enabled  = stops.size >= 2 && !isLoading,  // use original stops for count
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isLoading)
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp), strokeWidth = 2.dp,
                                color    = MaterialTheme.colorScheme.onPrimary
                            )
                        else
                            Text("Bereken route")
                    }
                    OutlinedButton(
                        onClick  = { viewModel.clearAll() },
                        modifier = Modifier.weight(1f)
                    ) { Text("Wissen") }
                }

                route?.let {
                    Text(
                        text  = "Route: %.1f km  |  ~%.0f min  |  ~%.0f g CO₂"
                            .format(it.totalDistance, it.estimatedTimeMin, it.totalCO2Grams),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
