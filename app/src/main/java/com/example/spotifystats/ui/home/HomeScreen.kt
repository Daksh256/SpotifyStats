package com.example.spotifystats.ui.home

import android.R.attr.onClick
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import com.example.spotifystats.ui.home.ArtistCard

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: StatsViewModel = viewModel()
){
    val context = LocalContext.current

    val sharedPreferences = context.getSharedPreferences("SpotifyStatsPrefs", Context.MODE_PRIVATE)
    val accessToken = sharedPreferences.getString("ACCESS_TOKEN", "") ?: ""

    val currentRange by viewModel.selectedTimeRange.collectAsState()
    val artists by viewModel.artists.collectAsState()
    val tracks by viewModel.tracks.collectAsState()
    val topGenres by viewModel.topGenres.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    val currentlyPlaying by viewModel.currentlyPlaying.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    LaunchedEffect(Unit) {
        if (accessToken.isNotEmpty()) {
            viewModel.fetchCurrentlyPlaying(accessToken)
            viewModel.fetchTopArtists(accessToken,currentRange)
            viewModel.fetchTopTracks(accessToken,currentRange)
            viewModel.fetchRecentlyPlayed(accessToken)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        TimeRangeTabs(
            selectedRange = currentRange,
            onRangeSelected = { newApiString ->
                viewModel.updateTimeRange(newApiString, accessToken)
            }
        )
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                currentlyPlaying?.let { track ->
                    NowPlayingCard(track = track, isPlaying = isPlaying)
                }
            }
            item { ArtistRow(title = "Your Top Artists", artists = artists,
                onArtistClick = { artist ->
                    viewModel.selectArtist(artist)
                    navController.navigate("artist_detail")
                }) }
            item { TrackRow(title = "Top Songs", tracks = tracks,
                onTrackClick = { track ->
                viewModel.selectTrack(track)
                navController.navigate("track_detail")
            }) }
            item { GenreRow(topGenres) }
            item { TrackRow(title = "Recently Played", tracks = recentlyPlayed,
                onTrackClick = { track ->
                viewModel.selectTrack(track)
                navController.navigate("track_detail")
            }) }
        }
    }
}

@Composable
fun ArtistRow(
    title: String,
    artists: List<Artist>,
    onArtistClick: (Artist) -> Unit
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
            items(artists) { artist ->
                ArtistCard(artist = artist, onClick = { onArtistClick(artist) })
            }
        }
    }
}

@Composable
fun ArtistCard(artist: Artist, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp).clickable { onClick() }

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
    tracks: List<Track>,
    onTrackClick: (Track) -> Unit
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
                TrackCard(track = track, onClick = { onTrackClick(track) })
            }
        }
    }
}

@Composable
fun TrackCard(track: Track, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.width(120.dp).clickable { onClick() }
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


@Composable
fun NowPlayingCard(track: Track, isPlaying: Boolean) {
    androidx.compose.foundation.layout.Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E1E))
            .padding(12.dp)
    ) {
        if (track.album.images.isNotEmpty()) {
            AsyncImage(
                model = track.album.images[0].url,
                contentDescription = "Album cover",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "NOW PLAYING",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF1DB954),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = track.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (track.artists.isNotEmpty()) {
                Text(
                    text = track.artists[0].name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Icon(
            painter = painterResource(
                id = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
            ),
            contentDescription = if (isPlaying) "Playing" else "Paused",
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun TimeRangeTabs(
    selectedRange: String,
    onRangeSelected: (String) -> Unit
) {
    val tabs = listOf(
        "1 Month" to "short_term",
        "6 Months" to "medium_term",
        "Lifetime" to "long_term"
    )

    val selectedIndex = tabs.indexOfFirst { it.second == selectedRange }.coerceAtLeast(0)

    TabRow(selectedTabIndex = selectedIndex) {
        tabs.forEachIndexed { index, (title, apiValue) ->
            Tab(
                selected = selectedIndex == index,
                onClick = { onRangeSelected(apiValue) },
                text = { Text(title) }
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