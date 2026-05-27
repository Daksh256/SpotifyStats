package com.example.spotifystats.ui.recap

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController


@Composable
fun RecapScreen(
    navController: NavController,
    viewModel: RecapViewModel = viewModel()
) {


    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("SpotifyStatsPrefs", Context.MODE_PRIVATE)
    val userId = sharedPreferences.getString("USER_ID", "") ?: ""

    val accessToken = sharedPreferences.getString("ACCESS_TOKEN", "") ?: ""

    LaunchedEffect(Unit) {
        if (userId.isNotEmpty()) viewModel.fetchRecap(userId, accessToken)
    }

    val isLoading by viewModel.isLoading.collectAsState()
    val recapMonth by viewModel.recapMonth.collectAsState()
    val recapYear by viewModel.recapYear.collectAsState()
    val totalMinutes by viewModel.totalMinutes.collectAsState()
    val totalStreams by viewModel.totalStreams.collectAsState()
    val totalHours by viewModel.totalHours.collectAsState()
    val topDay by viewModel.topDay.collectAsState()
    val totalArtists by viewModel.totalArtists.collectAsState()


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = SpotifyGreen,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {

                Text(
                    text = "$recapMonth $recapYear RECAP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SpotifyGreen,
                    letterSpacing = 0.1.sp
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "You listened for",
                        fontSize = 13.sp,
                        color = TextDimmed,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "$totalMinutes",
                        fontSize = 72.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SpotifyGreen,
                        lineHeight = 72.sp
                    )

                    Text(
                        text = "minutes",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Text(
                        text = "across $totalStreams streams this month",
                        fontSize = 14.sp,
                        color = TextGray,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(CardBg)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "QUICK STATS",
                            fontSize = 10.sp,
                            color = TextDimmed,
                            letterSpacing = 0.08.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            QuickStatBox(value = "$totalStreams", label = "Streams")
                            QuickStatBox(value = String.format("%.1fh", totalHours), label = "Hours")
                            QuickStatBox(value = topDay, label = "Top day")
                            QuickStatBox(value = "$totalArtists", label = "Artists")
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .border(2.dp, color = Color.White, shape = RoundedCornerShape(20.dp))
                            .clickable { navController.navigate("recap_artists") }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Top Artist →",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickStatBox(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = SpotifyGreen
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = TextDimmed,
            modifier = Modifier.padding(top = 3.dp)
        )
    }
}