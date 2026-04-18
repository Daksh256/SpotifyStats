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

@Composable
fun AppNavigation(viewModel: StatsViewModel){
    val navController = rememberNavController()
    val sharedPreferences = LocalContext.current.getSharedPreferences("SpotifyStatsPrefs", Context.MODE_PRIVATE)

    val savedToken = sharedPreferences.getString("REFRESH_TOKEN", null)

    val startScreen = if (savedToken != null) "MainScreen" else "LoginScreen"

    NavHost(
        navController = navController,
        startDestination = startScreen,
        builder = {
            composable("LoginScreen"){
                LoginScreen(navController)
            }
            composable("MainScreen"){
                MainScreen(viewModel = viewModel)
            }

        }

    )
}
