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
    val shareableView = remember { mutableStateOf<View?>(null) }

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

            AndroidView(
                factory = { ctx ->
                    android.widget.LinearLayout(ctx).apply {
                        orientation = android.widget.LinearLayout.VERTICAL
                        shareableView.value = this
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "$totalMinutes" to "Minutes",
                    "$totalStreams" to "Streams",
                    topDay to "Top day",
                    (topGenres.firstOrNull()?.replaceFirstChar { it.uppercase() } ?: "--") to "Top genre"
                ).forEach { (value, label) ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CardBg)
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = value,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SpotifyGreen,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = label.uppercase(),
                            fontSize = 9.sp,
                            color = TextDimmed,
                            modifier = Modifier.padding(top = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            SummarySection(title = "Top Artists") {
                topArtists.forEachIndexed { i, artist ->
                    SummaryRow(rank = i + 1, name = artist.artistName, value = "${artist.playCount} plays")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            SummarySection(title = "Top Songs") {
                topTracks.forEachIndexed { i, track ->
                    SummaryRow(
                        rank = i + 1,
                        name = track.trackName,
                        value = "${track.playCount}x",
                        subtitle = track.artistName
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SpotifyGreen)
                    .clickable { shareRecap(context) }
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

                Text(
                    text = "Tap above to share ↑",
                    fontSize = 11.sp,
                    color = TextDimmed
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun SummarySection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardBg)
            .padding(14.dp)
    ) {
        Text(
            text = title.uppercase(),
            fontSize = 10.sp,
            color = TextDimmed,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.08.sp,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        content()
    }
}

@Composable
private fun SummaryRow(
    rank: Int,
    name: String,
    value: String,
    subtitle: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "$rank.",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (rank == 1) SpotifyGreen else TextDimmed,
            modifier = Modifier.width(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 13.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = TextGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Text(
            text = value,
            fontSize = 12.sp,
            color = SpotifyGreen,
            fontWeight = FontWeight.Bold
        )
    }
    if (rank < 5) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(CardBg2)
        )
    }
}

fun shareRecap(context: Context) {
    try {

        val shareText = "Check out my Spotify recap on SpotifyStats!"

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, "Share your recap"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun shareRecapAsImage(context: Context, view: View) {
    try {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)

        val file = File(context.cacheDir, "recap.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share your recap"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}