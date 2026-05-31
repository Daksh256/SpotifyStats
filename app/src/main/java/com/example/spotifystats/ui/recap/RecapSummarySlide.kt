package com.example.spotifystats.ui.recap

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun RecapSummarySlide(
    navController: NavController,
    viewModel: RecapViewModel
) {
    val totalMinutes by viewModel.totalMinutes.collectAsState()
    val totalStreams by viewModel.totalStreams.collectAsState()
    val topDay by viewModel.topDay.collectAsState()
    val topArtists by viewModel.topArtists.collectAsState()
    val topTracks by viewModel.topTracks.collectAsState()
    val topGenres by viewModel.topGenres.collectAsState()
    val recapMonth by viewModel.recapMonth.collectAsState()
    val recapYear by viewModel.recapYear.collectAsState()

    val context = LocalContext.current
    val view = LocalView.current

    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "$recapMonth $recapYear · FULL RECAP",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SpotifyGreen,
                letterSpacing = 0.1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your month in music",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            ShareableRecapCard(
                recapMonth = recapMonth,
                recapYear = recapYear,
                totalMinutes = totalMinutes,
                totalStreams = totalStreams,
                topDay = topDay,
                topGenres = topGenres,
                topArtists = topArtists,
                topTracks = topTracks
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SpotifyGreen)
                    .clickable {
                        scope.launch {
                            captureAndShare(context, view, "$recapMonth $recapYear")
                        }
                    }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Share your recap",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF2A2A2A))
                        .clickable { navController.popBackStack() }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "← Back",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFAAAAAA) // Light gray text
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun ShareableRecapCard(
    recapMonth: String,
    recapYear: Int,
    totalMinutes: Int,
    totalStreams: Int,
    topDay: String,
    topGenres: List<String>,
    topArtists: List<RecapViewModel.RecapArtist>,
    topTracks: List<RecapViewModel.RecapTrack>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF111111))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$recapMonth $recapYear",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = SpotifyGreen
            )
            Text(
                text = "SpotifyStats",
                fontSize = 11.sp,
                color = TextDimmed
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "$totalMinutes" to "Minutes",
                "$totalStreams" to "Streams",
                topDay to "Top day",
                (topGenres.firstOrNull()?.replaceFirstChar { it.uppercase() } ?: "--") to "Genre"
            ).forEach { (value, label) ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1A1A1A))
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = value,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SpotifyGreen,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = label.uppercase(),
                        fontSize = 8.sp,
                        color = TextDimmed,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "TOP ARTISTS",
            fontSize = 9.sp,
            color = TextDimmed,
            letterSpacing = 0.08.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        topArtists.take(3).forEachIndexed { i, artist ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${i + 1}. ${artist.artistName}",
                    fontSize = 12.sp,
                    color = if (i == 0) Color.White else Color(0xFFAAAAAA),
                    fontWeight = if (i == 0) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = "${artist.playCount} plays",
                    fontSize = 11.sp,
                    color = SpotifyGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "TOP SONGS",
            fontSize = 9.sp,
            color = TextDimmed,
            letterSpacing = 0.08.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        topTracks.take(3).forEachIndexed { i, track ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${i + 1}. ${track.trackName}",
                    fontSize = 12.sp,
                    color = if (i == 0) Color.White else Color(0xFFAAAAAA),
                    fontWeight = if (i == 0) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.weight(1f),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
                Text(
                    text = "${track.playCount}x",
                    fontSize = 11.sp,
                    color = SpotifyGreen,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Made with SpotifyStats ♪",
                fontSize = 10.sp,
                color = TextDimmed
            )
        }
    }
}

suspend fun captureAndShare(context: Context, view: View, recapDate: String) {
    try {
        val bitmap = Bitmap.createBitmap(
            view.width, view.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)

        val uri = withContext(Dispatchers.IO) {
            val file = File(context.cacheDir, "recap_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "My $recapDate music recap! 🎵")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share your recap"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}