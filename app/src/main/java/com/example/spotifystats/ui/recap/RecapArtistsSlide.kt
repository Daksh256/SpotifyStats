package com.example.spotifystats.ui.recap

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
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
fun RecapArtistsSlide(
    navController: NavController,
    viewModel: RecapViewModel
) {
    val topArtists by viewModel.topArtists.collectAsState()
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
                text = "TOP ARTISTS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SpotifyGreen,
                letterSpacing = 0.1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your most played",
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
                topArtists.forEachIndexed { index, artist ->
                    ArtistRow(
                        rank = index + 1,
                        artist = artist
                    )
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
                        .clickable { navController.navigate("recap_artists") }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Top Tracks →",
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
private fun ArtistRow(rank: Int, artist: RecapViewModel.RecapArtist) {
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

        if (artist.imageUrl.isNotEmpty()) {
            AsyncImage(
                model = artist.imageUrl,
                contentDescription = artist.artistName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(CardBg2),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = artist.artistName.first().toString(),
                    color = TextGray,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = artist.artistName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1DB95420))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = "${artist.playCount} plays",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SpotifyGreen
            )
        }
    }
}