package com.example.spotifystats.ui.recap

import androidx.compose.foundation.layout.Box
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
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Your Wrap")
    }
}