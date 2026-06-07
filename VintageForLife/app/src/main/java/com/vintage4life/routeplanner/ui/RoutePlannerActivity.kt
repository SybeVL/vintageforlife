package com.vintage4life.routeplanner.ui

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import com.vintage4life.routeplanner.R
import com.vintage4life.routeplanner.viewmodel.RoutePlannerViewModel

/**
 * Entry point of the application.
 *
 * Responsibilities:
 *  - Request location permissions at startup
 *  - Initialise the [RoutePlannerViewModel] with the Mapbox access token
 *  - Hand off rendering to [RoutePlannerScreen]
 */
class RoutePlannerActivity : ComponentActivity() {

    private val viewModel: RoutePlannerViewModel by viewModels()

    private val locationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                          permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (granted) loadScreen()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun loadScreen() {
        viewModel.init(getString(R.string.mapbox_access_token))
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                RoutePlannerScreen(viewModel = viewModel)
            }
        }
    }
}
