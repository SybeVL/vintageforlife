package com.vintage4life.routeplanner.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.vintage4life.routeplanner.viewmodel.RoutePlannerViewModel

class RoutePlannerActivity : ComponentActivity() {

    private val viewModel: RoutePlannerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    RoutePlannerScreen(viewModel = viewModel)
                }
            }
        }
    }
}
