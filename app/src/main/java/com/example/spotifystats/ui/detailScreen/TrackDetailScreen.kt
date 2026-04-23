package com.example.spotifystats.ui.detailScreen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.spotifystats.ui.home.StatsViewModel
import androidx.compose.ui.text.style.TextAlign


@Composable
fun TrackDetailScreen(navController: NavController, viewModel: StatsViewModel) {
    val trackState by viewModel.selectedTrack.collectAsState()

    // 👉 1. Grab all top tracks so we can find the rank
    val allTracks by viewModel.tracks.collectAsState()

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    trackState?.let { track ->

        // 👉 2. Calculate Rank
        val rankIndex = allTracks.indexOfFirst { it.id == track.id }
        val rank = if (rankIndex >= 0) "#${rankIndex + 1}" else ">50"

        // 👉 3. Format Duration (Convert milliseconds to mm:ss)
        val minutes = track.duration_ms / 1000 / 60
        val seconds = (track.duration_ms / 1000 % 60).toString().padStart(2, '0')
        val formattedDuration = "$minutes:$seconds"

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (track.album.images.isNotEmpty()) {
                AsyncImage(
                    model = track.album.images[0].url,
                    contentDescription = "Album Cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .graphicsLayer {
                            translationY = scrollState.value * 0.5f
                        }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = track.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                if (track.artists.isNotEmpty()) {
                    Text(
                        text = "By ${track.artists[0].name}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // 👉 4. STATS DASHBOARD: Display Rank and Duration
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatBox(title = "Your Rank", value = rank)
                    StatBox(title = "Duration", value = formattedDuration)
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .clickable {
                            viewModel.selectAlbum(track.album)
                            navController.navigate("album_detail")
                        }
                        .padding(16.dp)
                ) {
                    Text(
                        text = "APPEARS ON",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF1DB954),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (track.album.images.isNotEmpty()) {
                        AsyncImage(
                            model = track.album.images[0].url,
                            contentDescription = "Album Art",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = track.album.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "View Album Details",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("spotify:search:${track.name}"))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954)),
                    modifier = Modifier.fillMaxWidth(0.8f).height(50.dp)
                ) {
                    Text("Play on Spotify", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }
}


// NOTE: If you put ArtistDetailScreen and TrackDetailScreen in two different files,
// you will need to copy the StatBox component here too! If they are in the same file,
// you only need it once at the bottom of the file.