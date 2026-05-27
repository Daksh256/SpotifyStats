package com.example.spotifystats.ui.recap

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController


@Composable
fun RecapGenresSlide(
    navController: NavController,
    viewModel: RecapViewModel
) {
    val topGenres by viewModel.topGenres.collectAsState()
    val recapMonth by viewModel.recapMonth.collectAsState()
    val recapYear by viewModel.recapYear.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            Text(
                text = "TOP GENRES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SpotifyGreen,
                letterSpacing = 0.1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your sound",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "$recapMonth $recapYear",
                fontSize = 12.sp,
                color = TextDimmed,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                topGenres.firstOrNull()?.let { topGenre ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF0D2818))
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "YOUR TOP GENRE",
                            fontSize = 10.sp,
                            color = Color(0x801DB954),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.08.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = topGenre.replaceFirstChar { it.uppercase() },
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SpotifyGreen
                        )
                        Text(
                            text = "Your most played genre",
                            fontSize = 12.sp,
                            color = Color(0x801DB954),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                if (topGenres.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(CardBg)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "GENRE BREAKDOWN",
                            fontSize = 10.sp,
                            color = TextDimmed,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.08.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        topGenres.forEachIndexed { index, genre ->
                            val fraction = 1f - (index * 0.18f)
                            val percent = (fraction * 100).toInt()
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = genre.replaceFirstChar { it.uppercase() },
                                        fontSize = 12.sp,
                                        color = if (index == 0) SpotifyGreen else Color(0xFFAAAAAA)
                                    )
                                    Text(
                                        text = "$percent%",
                                        fontSize = 12.sp,
                                        color = SpotifyGreen,
                                        fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(CardBg2)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(fraction)
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(SpotifyGreen)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(CardBg2)
                        .clickable { navController.popBackStack() }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "← Back",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGray
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(SpotifyGreen)
                        .clickable { navController.navigate("recap_active_day") }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Most Active Day →",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}