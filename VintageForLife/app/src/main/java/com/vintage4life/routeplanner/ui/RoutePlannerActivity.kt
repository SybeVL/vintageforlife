package com.vintage4life.routeplanner.ui

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import com.mapbox.common.MapboxOptions
import com.vintage4life.routeplanner.BuildConfig
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
            else {
                Toast.makeText(this, "Location permission required for map", Toast.LENGTH_LONG).show()
                loadScreen() // Still load screen to show map, even if location isn't centered
            }
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
        // Trace the token through multiple sources to ensure it's found
        val tokenFromRes = try { getString(R.string.mapbox_access_token) } catch (e: Exception) { "" }
        val tokenFromBuildConfig = BuildConfig.MAPBOX_ACCESS_TOKEN
        
        // Final token selection logic
        val token = when {
            tokenFromRes.isNotBlank() && !tokenFromRes.contains("MAPBOX_ACCESS_TOKEN") -> tokenFromRes
            tokenFromBuildConfig.isNotBlank() && !tokenFromBuildConfig.contains("MAPBOX_ACCESS_TOKEN") -> tokenFromBuildConfig
            else -> ""
        }

        if (token.isBlank()) {
            Log.e("RoutePlannerActivity", "FATAL: Mapbox token is missing in both Resources and BuildConfig!")
            Log.d("RoutePlannerActivity", "Resource value: '$tokenFromRes'")
            Log.d("RoutePlannerActivity", "BuildConfig value: '$tokenFromBuildConfig'")
            Toast.makeText(this, "Mapbox token missing! Check local.properties and Rebuild.", Toast.LENGTH_LONG).show()
        } else {
            Log.d("RoutePlannerActivity", "Mapbox token loaded successfully (length: ${token.length})")
            MapboxOptions.accessToken = token
            viewModel.init(token)
        }

        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                RoutePlannerScreen(viewModel = viewModel)
            }
        }
    }
}
