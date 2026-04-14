package com.example.spotifystats.ui.home

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.spotifystats.R
import com.example.spotifystats.data.Artist
import com.example.spotifystats.ui.theme.SpotifyStatsTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.spotifystats.data.Track
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: StatsViewModel = viewModel()
){
    val context = LocalContext.current

    val sharedPreferences = context.getSharedPreferences("SpotifyStatsPrefs", Context.MODE_PRIVATE)
    val accessToken = sharedPreferences.getString("ACCESS_TOKEN", "") ?: ""

    val artists by viewModel.artists.collectAsState()
    val tracks by viewModel.tracks.collectAsState()
    val topGenres by viewModel.topGenres.collectAsState()

    LaunchedEffect(Unit) {
        if (accessToken.isNotEmpty()) {
            viewModel.fetchTopArtists(accessToken)
            viewModel.fetchTopTracks(accessToken)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item { ArtistRow(title = "Your Top Artists", artists = artists) }
            item { TrackRow(title = "Top Songs", tracks = tracks) }
            item { GenreRow(topGenres) }
        }
    }
}

@Composable
fun ArtistRow(
    title: String,
    artists: List<Artist>
) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(artists) { artist ->
                ArtistCard(artist = artist)
            }
        }
    }
}

@Composable
fun ArtistCard(artist: Artist) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        if (artist.images.isNotEmpty()) {
            AsyncImage(
                model = artist.images[0].url,
                contentDescription = "Image of ${artist.name}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = artist.name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TrackRow(
    title: String,
    tracks: List<Track>
) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 16.dp, bottom = 12.dp, end = 16.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(tracks) { track ->
                TrackCard(track = track)
            }
        }
    }
}

@Composable
fun TrackCard(track: Track) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.width(120.dp)
    ) {
        if (track.album.images.isNotEmpty()) {
            AsyncImage(
                model = track.album.images[0].url,
                contentDescription = "Album cover for ${track.name}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.DarkGray)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = track.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (track.artists.isNotEmpty()) {
            Text(
                text = track.artists[0].name,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun GenreRow(
    genres: List<String>
) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
            text = "Your Top Genres",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp, start = 16.dp , end = 16.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(genres) { genre ->
                Text(
                    text = genre.uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .background(Color(0xFF1DB954), shape = CircleShape)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
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