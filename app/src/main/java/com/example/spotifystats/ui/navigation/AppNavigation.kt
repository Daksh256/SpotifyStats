package com.example.spotifystats.ui.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.spotifystats.MainScreen
import com.example.spotifystats.ui.home.HomeScreen
import com.example.spotifystats.ui.home.StatsViewModel
import com.example.spotifystats.ui.login.LoginScreen


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.spotifystats.MainScreen
import com.example.spotifystats.ui.detailScreen.AlbumDetailScreen
import com.example.spotifystats.ui.detailScreen.ArtistDetailScreen
import com.example.spotifystats.ui.detailScreen.TrackDetailScreen
//import com.example.spotifystats.ui.home.StatsViewModel
import com.example.spotifystats.ui.login.LoginScreen

@Composable
fun AppNavigation(viewModel: StatsViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("SpotifyStatsPrefs", Context.MODE_PRIVATE)

    val savedRefreshToken = sharedPreferences.getString("REFRESH_TOKEN", null)

    var isRefreshing by remember { mutableStateOf(savedRefreshToken != null) }
    var hasValidAccessToken by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (savedRefreshToken != null) {
            val newAccessToken = viewModel.refreshSpotifyToken(savedRefreshToken)

            if (newAccessToken != null) {
                sharedPreferences.edit().putString("ACCESS_TOKEN", newAccessToken).apply()
                hasValidAccessToken = true
            } else {
                sharedPreferences.edit().clear().apply()
                hasValidAccessToken = false
            }
        } else {
            hasValidAccessToken = false
        }

        isRefreshing = false
    }

    if (isRefreshing) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        val startScreen = if (hasValidAccessToken) "MainScreen" else "LoginScreen"

        NavHost(
            navController = navController,
            startDestination = startScreen,
            builder = {
                composable("LoginScreen") {
                    LoginScreen(navController)
                }

                composable("MainScreen") {
                    MainScreen(
                        viewModel = viewModel,
                        onLogout = {
                            navController.navigate("LoginScreen") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }

                composable("MainScreen") {
                    MainScreen(
                        viewModel = viewModel,
                        onLogout = {
                            navController.navigate("LoginScreen") {
                                // This clears the entire app history so they can't hit the back button to get back in!
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
        )
    }
}