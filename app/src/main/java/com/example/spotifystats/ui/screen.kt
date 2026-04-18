package com.example.spotifystats.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Stats", Icons.Default.Home)
    object Recap : Screen("recap", "Recap", Icons.Default.Star)
}