package com.example.spotifystats.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.example.spotifystats.ui.theme.SpotifyStatsTheme


@Composable
fun HomeScreen(
    navController: NavController
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "This is the HomeScreen"
        )
    }
}

@Preview(
    showBackground = true
)
@Composable
fun HomeScreenPreview(){
    SpotifyStatsTheme() {
        HomeScreen(
            navController = androidx.navigation.compose.rememberNavController()
        )
    }
}