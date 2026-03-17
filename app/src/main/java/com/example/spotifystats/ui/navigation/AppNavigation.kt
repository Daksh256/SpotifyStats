package com.example.spotifystats.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.spotifystats.ui.home.HomeScreen
import com.example.spotifystats.ui.login.LoginScreen

@Composable
fun AppNavigation(){
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "LoginScreen",
        builder = {
            composable("LoginScreen"){
                LoginScreen(navController)
            }
            composable("HomeScreen"){
                HomeScreen(navController)
            }
        }

    )
}
