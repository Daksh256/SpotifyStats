package com.example.spotifystats.ui.recap

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage

private val SpotifyGreen = Color(0xFF1DB954)
private val DarkBg = Color(0xFF0A0A0A)
private val CardBg = Color(0xFF111111)
private val CardBg2 = Color(0xFF1A1A1A)
private val TextGray = Color(0xFF888888)
private val TextDimmed = Color(0xFF555555)

@Composable
fun RecapTracksSlide(
    navController: NavController,
    viewModel: RecapViewModel
) {
    val topTracks by viewModel.topTracks.collectAsState()
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
                text = "TOP SONGS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SpotifyGreen,
                letterSpacing = 0.1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your anthems",
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                topTracks.forEachIndexed { index, track ->
                    TrackRow(rank = index + 1, track = track)
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
                        .border(2.dp, color = Color.White, shape = RoundedCornerShape(20.dp))
                        .clickable { navController.popBackStack() }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "← Back",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .border(2.dp, color = Color.White, shape = RoundedCornerShape(20.dp))
                        .clickable { navController.navigate("recap_genres") }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Top Genres →",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackRow(rank: Int, track: RecapViewModel.RecapTrack) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBg)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "$rank",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (rank == 1) SpotifyGreen else TextDimmed,
            modifier = Modifier.width(24.dp)
        )

        if (track.albumArt.isNotEmpty()) {
            AsyncImage(
                model = track.albumArt,
                contentDescription = track.trackName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CardBg2),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "♪",
                    color = TextGray,
                    fontSize = 18.sp
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.trackName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.artistName,
                fontSize = 12.sp,
                color = TextGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1DB95420))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = "${track.playCount}x",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SpotifyGreen
            )
        }
    }
}