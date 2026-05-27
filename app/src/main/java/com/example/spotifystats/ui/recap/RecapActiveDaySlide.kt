package com.example.spotifystats.ui.recap

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
fun RecapActiveDaySlide(
    navController: NavController,
    viewModel: RecapViewModel
) {
    val topDay by viewModel.topDay.collectAsState()
    val minutesByDay by viewModel.minutesByDay.collectAsState()
    val recapMonth by viewModel.recapMonth.collectAsState()
    val recapYear by viewModel.recapYear.collectAsState()

    val maxMinutes = minutesByDay.values.maxOrNull()?.takeIf { it > 0 } ?: 1
    val topDayMinutes = minutesByDay[topDay] ?: 0

    val orderedDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            Text(
                text = "MOST ACTIVE DAY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SpotifyGreen,
                letterSpacing = 0.1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "When you vibe",
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0D2818))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "YOUR MOST ACTIVE DAY",
                        fontSize = 10.sp,
                        color = Color(0x801DB954),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.08.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when (topDay) {
                            "Mon" -> "Monday"
                            "Tue" -> "Tuesday"
                            "Wed" -> "Wednesday"
                            "Thu" -> "Thursday"
                            "Fri" -> "Friday"
                            "Sat" -> "Saturday"
                            "Sun" -> "Sunday"
                            else -> topDay
                        },
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SpotifyGreen
                    )
                    Text(
                        text = "$topDayMinutes minutes of music",
                        fontSize = 13.sp,
                        color = Color(0x801DB954),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                Text(
                    text = "MINUTES BY DAY OF WEEK",
                    fontSize = 10.sp,
                    color = TextDimmed,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.08.sp
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CardBg)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    orderedDays.forEach { day ->
                        val minutes = minutesByDay[day] ?: 0
                        val fraction = minutes.toFloat() / maxMinutes
                        val isPeak = day == topDay

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = when (day) {
                                        "Mon" -> "Monday"
                                        "Tue" -> "Tuesday"
                                        "Wed" -> "Wednesday"
                                        "Thu" -> "Thursday"
                                        "Fri" -> "Friday"
                                        "Sat" -> "Saturday"
                                        "Sun" -> "Sunday"
                                        else -> day
                                    },
                                    fontSize = 12.sp,
                                    color = if (isPeak) SpotifyGreen else Color(0xFFAAAAAA),
                                    fontWeight = if (isPeak) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = "$minutes min",
                                    fontSize = 12.sp,
                                    color = if (isPeak) SpotifyGreen else TextGray,
                                    fontWeight = if (isPeak) FontWeight.Bold else FontWeight.Normal
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
            Spacer(Modifier.padding(top = 12.dp))

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
                        .clickable { navController.navigate("recap_summary") }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Summary →",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}