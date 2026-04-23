package com.example.spotifystats

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.spotifystats.ui.Screen
import com.example.spotifystats.ui.SettingsScreen
import com.example.spotifystats.ui.detailScreen.AlbumDetailScreen
import com.example.spotifystats.ui.detailScreen.ArtistDetailScreen
import com.example.spotifystats.ui.detailScreen.TrackDetailScreen
import com.example.spotifystats.ui.home.HomeScreen
import com.example.spotifystats.ui.home.StatsViewModel
import com.example.spotifystats.ui.recap.RecapScreen

@Composable
fun MainScreen(viewModel: StatsViewModel, onLogout: () -> Unit) {
    val bottomNavController = rememberNavController()

    val items = listOf(
        Screen.Home,
        Screen.Recap,
        Screen.Settings
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            bottomNavController.navigate(screen.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = bottomNavController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable(Screen.Home.route) {
                HomeScreen(
                    navController = bottomNavController,
                    viewModel = viewModel
                )
            }

            composable(Screen.Recap.route) {
                RecapScreen()
            }

            composable("artist_detail") {
                ArtistDetailScreen(navController = bottomNavController, viewModel = viewModel)
            }

            composable("track_detail") {
                TrackDetailScreen(navController = bottomNavController, viewModel = viewModel)
            }

            composable("album_detail") {
                AlbumDetailScreen(navController = bottomNavController, viewModel = viewModel)
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel,
                    onLogoutClick = onLogout
                )
            }
        }
    }
}