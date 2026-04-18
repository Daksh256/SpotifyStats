package com.example.spotifystats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spotifystats.ui.home.StatsViewModel
import com.example.spotifystats.ui.navigation.AppNavigation
import com.example.spotifystats.ui.theme.SpotifyStatsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpotifyStatsTheme {

                // 👉 1. Create the ViewModel here at the very top of the app
                val viewModel: StatsViewModel = viewModel()

                // 👉 2. Pass it down into your Navigation engine
                AppNavigation(viewModel = viewModel)

            }
        }
    }
}

