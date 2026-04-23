package com.example.spotifystats.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.spotifystats.ui.home.StatsViewModel

@Composable
fun SettingsScreen(viewModel: StatsViewModel, onLogoutClick: () -> Unit) {
    val context = LocalContext.current

    val userProfile by viewModel.userProfile.collectAsState()

    val sharedPreferences = context.getSharedPreferences("SpotifyStatsPrefs", android.content.Context.MODE_PRIVATE)
    val accessToken = sharedPreferences.getString("ACCESS_TOKEN", null)

    LaunchedEffect(Unit) {
        if (accessToken != null) {
            viewModel.fetchUserProfile(accessToken)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        userProfile?.let { profile ->
            if (profile.images.isNotEmpty()) {
                AsyncImage(
                    model = profile.images[1].url,
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier.size(150.dp).clip(CircleShape).background(Color.DarkGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Image", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = profile.display_name,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(48.dp))
        }

        Button(
            onClick = {
                viewModel.logout(context)
                onLogoutClick()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
            modifier = Modifier.fillMaxWidth(0.8f).height(50.dp)
        ) {
            Text("Log Out", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}