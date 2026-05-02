package com.example.spotifystats.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.spotifystats.ui.home.StatsViewModel

private val SpotifyGreen = Color(0xFF1DB954)
private val DarkBg = Color(0xFF0A0A0A)
private val CardBg = Color(0xFF111111)
private val CardBg2 = Color(0xFF1A1A1A)
private val TextGray = Color(0xFF888888)
private val TextDimmed = Color(0xFF555555)
private val DangerRed = Color(0xFFFF4444)

@Composable
fun SettingsScreen(
    viewModel: StatsViewModel,
    onLogoutClick: () -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("SpotifyStatsPrefs", Context.MODE_PRIVATE)
    val accessToken = sharedPreferences.getString("ACCESS_TOKEN", null)
    val userProfile by viewModel.userProfile.collectAsState()
    val minutesThisMonth by viewModel.minutesThisMonth.collectAsState()
    val userId = sharedPreferences.getString("USER_ID", "") ?: ""

    LaunchedEffect(Unit) {
        if (accessToken != null) {
            viewModel.fetchUserProfile(accessToken)
            if (userId.isNotEmpty()) viewModel.fetchMinutesThisMonth(userId)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Profile header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 52.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (userProfile?.images?.isNotEmpty() == true) {
                    AsyncImage(
                        model = userProfile!!.images[0].url,
                        contentDescription = "Profile picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(SpotifyGreen),
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
                            fontSize = 32.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = userProfile?.display_name ?: "Loading...",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "@${userId.take(8)}",
                    fontSize = 13.sp,
                    color = TextGray,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .background(Color(0xFF1DB95420), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Spotify Connected",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SpotifyGreen
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatBox(modifier = Modifier.weight(1f), value = "Apr", label = "Since")
                StatBox(modifier = Modifier.weight(1f), value = "${minutesThisMonth ?: "--"}", label = "Minutes")
                StatBox(modifier = Modifier.weight(1f), value = "15m", label = "Sync rate")
            }
        }

        // Account section
        item { SectionLabel("Account") }
        item {
            MenuCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                MenuItem(
                    iconColor = Color(0xFF1DB95420),
                    iconContent = {
                        Text("♪", color = SpotifyGreen, fontSize = 16.sp)
                    },
                    label = "Spotify account",
                    value = "Connected"
                )
                MenuItem(
                    iconColor = CardBg2,
                    iconContent = {
                        Text("◷", color = TextGray, fontSize = 16.sp)
                    },
                    label = "Listening since",
                    value = "April 2026"
                )
                MenuItem(
                    iconColor = CardBg2,
                    iconContent = {
                        Text("≡", color = TextGray, fontSize = 16.sp)
                    },
                    label = "Total streams",
                    value = "59 tracks",
                    showDivider = false
                )
            }
        }

        item { SectionLabel("Preferences") }
        item {
            MenuCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                MenuItem(
                    iconColor = CardBg2,
                    iconContent = { Text("🔔", color = TextGray, fontSize = 13.sp) },
                    label = "Sync frequency",
                    value = "Every 15 min",
                    showDivider = false
                )
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF1A0A0A))
                    .clickable {
                        viewModel.logout(context)
                        onLogoutClick()
                    }
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x22FF4444)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("→", color = DangerRed, fontSize = 16.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Log out",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DangerRed
                        )
                        Text(
                            text = "You'll need to log in again",
                            fontSize = 12.sp,
                            color = TextDimmed,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Text("›", color = Color(0x66FF4444), fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
private fun StatBox(modifier: Modifier = Modifier, value: String, label: String) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CardBg)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = SpotifyGreen
        )
        Text(
            text = label.uppercase(),
            fontSize = 10.sp,
            color = TextDimmed,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 11.sp,
        color = TextDimmed,
        letterSpacing = 0.08.sp,
        modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
private fun MenuCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg),
        content = content
    )
}

@Composable
private fun MenuItem(
    iconColor: Color,
    iconContent: @Composable () -> Unit,
    label: String,
    value: String,
    showDivider: Boolean = true,
    showChevron: Boolean = false,
    valueColor: Color = TextGray
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconColor),
                contentAlignment = Alignment.Center
            ) { iconContent() }
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            Text(text = value, fontSize = 13.sp, color = valueColor)
            if (showChevron) {
                Text("›", fontSize = 16.sp, color = TextDimmed)
            }
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 64.dp),
                thickness = 0.5.dp,
                color = Color(0xFF1A1A1A)
            )
        }
    }
}