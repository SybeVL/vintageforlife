package com.vintage4life.routeplanner.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.vintage4life.routeplanner.model.Location
import com.vintage4life.routeplanner.model.OptimizationCriteria
import com.vintage4life.routeplanner.viewmodel.RoutePlannerViewModel

@Composable
fun RoutePlannerScreen(viewModel: RoutePlannerViewModel) {
    val stopList     by viewModel.stopList.collectAsState()
    val routeResult  by viewModel.routeResult.collectAsState()
    val isLoading    by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var nameInput    by remember { mutableStateOf("") }
    var addressInput by remember { mutableStateOf("") }
    var selectedCriteria by remember { mutableStateOf(OptimizationCriteria.DISTANCE) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ── Title ─────────────────────────────────────────────────────────────
        Text(
            text = "Routeplanner",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // ── Input fields ──────────────────────────────────────────────────────
        OutlinedTextField(
            value = nameInput,
            onValueChange = { nameInput = it },
            label = { Text("Stop name (optional)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = addressInput,
            onValueChange = { addressInput = it },
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Add stop button
        Button(
            onClick = {
                // PoC: coordinates hardcoded to 0,0 — replace with Mapbox Geocoding API call
                viewModel.addStop(
                    address = addressInput,
                    name = nameInput,
                    lat = 0.0,
                    lon = 0.0
                )
                addressInput = ""
                nameInput = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add stop")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Optimisation criteria ─────────────────────────────────────────────
        Text("Optimalisatiecriterium:", fontWeight = FontWeight.SemiBold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OptimizationCriteria.values().forEach { criteria ->
                RadioButton(
                    selected = selectedCriteria == criteria,
                    onClick = { selectedCriteria = criteria }
                )
                Text(
                    text = when (criteria) {
                        OptimizationCriteria.DISTANCE       -> "Distance"
                        OptimizationCriteria.TIME           -> "Time"
                        OptimizationCriteria.SUSTAINABILITY -> "Sustainability"
                    },
                    modifier = Modifier.padding(end = 12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── Action buttons ────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.solveRoute(selectedCriteria) },
                enabled = !isLoading,
                modifier = Modifier.weight(1f)
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp))
                else Text("Solve route")
            }
            OutlinedButton(
                onClick = { viewModel.clearAll() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Clear")
            }
        }

        // ── Error message ─────────────────────────────────────────────────────
        errorMessage?.let { msg ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = msg,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Mapbox kaart ──────────────────────────────────────────────────────
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ── Stop lijst ────────────────────────────────────────────────────────
        routeResult?.let {
            Text(
                text = "Route: %.1f km  |  ~%.0f min".format(it.totalDistanceKm, it.estimatedTimeMin),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            itemsIndexed(stopList) { index, stop ->
                StopListItem(
                    index = index + 1,
                    stop = stop,
                    onRemove = { viewModel.removeStop(stop) }
                )
            }
        }
    }
}

@Composable
private fun StopListItem(
    index: Int,
    stop: Location,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$index.",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(28.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                if (stop.name.isNotBlank()) {
                    Text(text = stop.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                Text(text = stop.address, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onRemove) {
                Text("✕", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
