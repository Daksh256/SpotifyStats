package com.example.spotifystats.ui.home

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.spotifystats.data.Artist
import com.example.spotifystats.data.Track
import kotlinx.coroutines.launch

private val SpotifyGreen = Color(0xFF1DB954)
private val DarkBg = Color(0xFF0A0A0A)
private val CardBg = Color(0xFF111111)
private val CardBg2 = Color(0xFF1A1A1A)
private val TextGray = Color(0xFF888888)
private val TextDimmed = Color(0xFF555555)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: StatsViewModel = viewModel()
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("SpotifyStatsPrefs", Context.MODE_PRIVATE)
    val accessToken = sharedPreferences.getString("ACCESS_TOKEN", "") ?: ""
    val userId = sharedPreferences.getString("USER_ID", "") ?: ""

    val currentRange by viewModel.selectedTimeRange.collectAsState()
    val artists by viewModel.artists.collectAsState()
    val tracks by viewModel.tracks.collectAsState()
    val topGenres by viewModel.topGenres.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    val currentlyPlaying by viewModel.currentlyPlaying.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val minutesThisMonth by viewModel.minutesThisMonth.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val trendData by viewModel.trendData.collectAsState()
    val trendRange by viewModel.trendRange.collectAsState()

    val scope = rememberCoroutineScope()
    var refreshing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (accessToken.isNotEmpty()) {
            viewModel.fetchCurrentlyPlaying(accessToken)
            viewModel.fetchTopArtists(accessToken, currentRange)
            viewModel.fetchTopTracks(accessToken, currentRange)
            viewModel.fetchRecentlyPlayed(accessToken)
            viewModel.fetchUserProfile(accessToken)
            if (userId.isNotEmpty()) viewModel.fetchMinutesThisMonth(userId)
            if (userId.isNotEmpty()) viewModel.fetchTrendData(userId)
        }
    }

    // Stop spinner when ViewModel finishes refreshing
    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) refreshing = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 52.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Good evening", fontSize = 12.sp, color = TextGray)
                        Text(
                            text = buildAnnotatedString {
                                append("Hey, ")
                                pushStyle(androidx.compose.ui.text.SpanStyle(color = SpotifyGreen))
                                append(userProfile?.display_name?.split(" ")?.firstOrNull() ?: "there")
                                pop()
                                append(" 👋")
                            },
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                refreshing = true
                                viewModel.refreshAll(accessToken, userId)
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CardBg)
                        ) {
                            if (refreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = SpotifyGreen,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh",
                                    tint = SpotifyGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(SpotifyGreen)
                                .clickable { navController.navigate("Profile") },
                            contentAlignment = Alignment.Center
                        ) {
                            val initials = userProfile?.display_name
                                ?.split(" ")
                                ?.mapNotNull { it.firstOrNull()?.toString() }
                                ?.take(2)
                                ?.joinToString("") ?: "?"
                            Text(
                                text = initials,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            item {
                currentlyPlaying?.let { track ->
                    NowPlayingCard(track = track, isPlaying = isPlaying)
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1DB95415))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "THIS MONTH",
                            fontSize = 10.sp,
                            color = TextGray,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${minutesThisMonth ?: "--"}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = SpotifyGreen,
                                lineHeight = 28.sp
                            )
                            Text(
                                text = " min",
                                fontSize = 13.sp,
                                color = TextGray,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                        Text(
                            text = "minutes listened",
                            fontSize = 11.sp,
                            color = TextDimmed,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    tracks.firstOrNull()?.let { topTrack ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(CardBg)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "TOP TRACK",
                                fontSize = 10.sp,
                                color = TextGray,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = topTrack.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 17.sp
                            )
                            Text(
                                text = topTrack.artists.firstOrNull()?.name ?: "",
                                fontSize = 11.sp,
                                color = TextDimmed,
                                modifier = Modifier.padding(top = 4.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            item {
                ListeningTrendChart(
                    trendData = trendData,
                    selectedRange = trendRange,
                    onRangeChange = { range ->
                        viewModel.setTrendRange(range, userId)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            item {
                TimeRangeTabs(
                    selectedRange = currentRange,
                    onRangeSelected = { viewModel.updateTimeRange(it, accessToken) }
                )
            }

            item {
                ArtistRow(
                    title = "Top Artists",
                    artists = artists,
                    onArtistClick = { artist ->
                        viewModel.selectArtist(artist)
                        navController.navigate("artist_detail")
                    }
                )
            }

            item {
                TrackRow(
                    title = "Top Songs",
                    tracks = tracks,
                    onTrackClick = { track ->
                        viewModel.selectTrack(track)
                        navController.navigate("track_detail")
                    }
                )
            }

            item { GenreRow(topGenres) }

            item {
                TrackRow(
                    title = "Recently Played",
                    tracks = recentlyPlayed,
                    onTrackClick = { track ->
                        viewModel.selectTrack(track)
                        navController.navigate("track_detail")
                    }
                )
            }
        }
    }
}

@Composable
fun NowPlayingCard(track: Track, isPlaying: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .padding(14.dp)
    ) {
        if (track.album.images.isNotEmpty()) {
            AsyncImage(
                model = track.album.images[0].url,
                contentDescription = "Album cover",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isPlaying) "▶  NOW PLAYING" else "LAST PLAYED",
                fontSize = 10.sp,
                color = SpotifyGreen,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = track.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.artists.firstOrNull()?.name ?: "",
                fontSize = 12.sp,
                color = TextGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(if (isPlaying) SpotifyGreen else CardBg2),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(
                    id = if (isPlaying) android.R.drawable.ic_media_pause
                    else android.R.drawable.ic_media_play
                ),
                contentDescription = null,
                tint = if (isPlaying) Color.Black else Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun TimeRangeTabs(selectedRange: String, onRangeSelected: (String) -> Unit) {
    val tabs = listOf(
        "1 Month" to "short_term",
        "6 Months" to "medium_term",
        "Lifetime" to "long_term"
    )
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEach { (label, value) ->
            val selected = selectedRange == value
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) Color.Black else TextGray,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (selected) SpotifyGreen else CardBg)
                    .clickable { onRangeSelected(value) }
                    .padding(horizontal = 16.dp, vertical = 7.dp)
            )
        }
    }
}

@Composable
fun ArtistRow(title: String, artists: List<Artist>, onArtistClick: (Artist) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = "See all", fontSize = 12.sp, color = SpotifyGreen)
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
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
        modifier = Modifier
            .width(76.dp)
            .clickable { onClick() }
    ) {
        if (artist.images.isNotEmpty()) {
            AsyncImage(
                model = artist.images[0].url,
                contentDescription = artist.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(CardBg2),
                contentAlignment = Alignment.Center
            ) {
                Text(text = artist.name.first().toString(), color = TextGray, fontSize = 20.sp)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = artist.name,
            fontSize = 11.sp,
            color = Color(0xFFAAAAAA),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TrackRow(title: String, tracks: List<Track>, onTrackClick: (Track) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = "See all", fontSize = 12.sp, color = SpotifyGreen)
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tracks) { track ->
                TrackCard(track = track, onClick = { onTrackClick(track) })
            }
        }
    }
}

@Composable
fun TrackCard(track: Track, onClick: () -> Unit) {
    Column(modifier = Modifier.width(110.dp).clickable { onClick() }) {
        if (track.album.images.isNotEmpty()) {
            AsyncImage(
                model = track.album.images[0].url,
                contentDescription = track.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardBg2)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = track.name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = track.artists.firstOrNull()?.name ?: "",
            fontSize = 11.sp,
            color = TextGray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun GenreRow(genres: List<String>) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = "Top Genres",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(genres) { genre ->
                Text(
                    text = genre.uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = SpotifyGreen,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF1DB95420))
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                )
            }
        }
    }
}