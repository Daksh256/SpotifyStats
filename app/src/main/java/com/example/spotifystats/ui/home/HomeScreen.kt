package com.example.spotifystats.ui.home

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.example.spotifystats.R
import com.example.spotifystats.ui.theme.SpotifyStatsTheme


@Composable
fun HomeScreen(
    navController: NavController
){

    val context = LocalContext.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "This is the HomeScreen"
        )

        Button(
            onClick = {
                val sharedPreferences = context.getSharedPreferences("SpotifyStatsPrefs", Context.MODE_PRIVATE)

                sharedPreferences.edit().remove("AUTH_CODE").apply()

                navController.navigate("LoginScreen") {
                    popUpTo(0) { inclusive = true }
                }
            }
        ) {
            Text(
                text = "Logout"
            )
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Logout your account"
            )
        }
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