package com.example.spotifystats.ui.recap

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState

import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun RecapScreen(
    viewModel: RecapViewModel
) {

    val totalMinutes by viewModel.totalMinutes.collectAsState()
    val totalStreams by viewModel.totalStreams.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentMonth by viewModel.recapMonth.collectAsState()
    val topDay by viewModel.topDay.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Your Wrap")

        Text(text = "Top Day: $topDay")

        Text(text = "Month: ${currentMonth.toString()}")
    }
}