package com.example.spotifystats.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val SpotifyGreen = Color(0xFF1DB954)
private val CardBg = Color(0xFF111111)
private val CardBg2 = Color(0xFF1A1A1A)
private val TextGray = Color(0xFF888888)
private val TextDimmed = Color(0xFF555555)
private val BarEmpty = Color(0xFF1A1A1A)
private val BarFilled = Color(0xFF0D5C2A)
private val BarPeak = Color(0xFF1DB954)

@Composable
fun ListeningTrendChart(
    trendData: Map<String, Int>,
    selectedRange: String,
    onRangeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val labels = trendData.keys.toList()
    val values = trendData.values.toList()
    val maxVal = values.maxOrNull()?.takeIf { it > 0 } ?: 1
    val total = values.sum()
    val peakIndex = values.indexOfFirst { it == values.maxOrNull() }
    val peakLabel = if (peakIndex >= 0 && labels.isNotEmpty()) labels[peakIndex] else "--"
    val avg = if (values.isNotEmpty()) values.sum() / values.size else 0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Listening Trend",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Total: ",
                fontSize = 12.sp,
                color = TextGray
            )
            Text(
                text = "$total min",
                fontSize = 12.sp,
                color = SpotifyGreen,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("7d" to "7 days", "30d" to "30 days", "dow" to "By day").forEach { (range, label) ->
                val selected = selectedRange == range
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) Color.Black else TextGray,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (selected) SpotifyGreen else CardBg2)
                        .clickable { onRangeChange(range) }
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (trendData.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No data yet", fontSize = 13.sp, color = TextDimmed)
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .width(28.dp)
                        .height(140.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(maxVal, maxVal / 2, 0).forEach { v ->
                        Text(
                            text = "${v}m",
                            fontSize = 9.sp,
                            color = TextDimmed
                        )
                    }
                }

                // Bar chart
                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp)
                ) {
                    val chartW = size.width
                    val chartH = size.height
                    val barCount = values.size
                    val gap = 4.dp.toPx()
                    val barW = (chartW - gap * (barCount - 1)) / barCount

                    listOf(0f, 0.5f, 1f).forEach { fraction ->
                        val y = chartH - (chartH * fraction)
                        drawLine(
                            color = Color(0xFF1A1A1A),
                            start = Offset(0f, y),
                            end = Offset(chartW, y),
                            strokeWidth = 0.5.dp.toPx()
                        )
                    }

                    values.forEachIndexed { i, value ->
                        val barH = (value.toFloat() / maxVal) * chartH
                        val x = i * (barW + gap)
                        val isPeak = i == peakIndex && value > 0
                        val isFilled = value > 0

                        drawRoundRect(
                            color = BarEmpty,
                            topLeft = Offset(x, 0f),
                            size = Size(barW, chartH),
                            cornerRadius = CornerRadius(4.dp.toPx())
                        )

                        if (barH > 0) {
                            drawRoundRect(
                                color = if (isPeak) BarPeak else BarFilled,
                                topLeft = Offset(x, chartH - barH),
                                size = Size(barW, barH),
                                cornerRadius = CornerRadius(4.dp.toPx())
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 28.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                labels.forEachIndexed { i, label ->
                    val isPeak = i == peakIndex && (values.getOrNull(i) ?: 0) > 0
                    Text(
                        text = label,
                        fontSize = 9.sp,
                        color = if (isPeak) SpotifyGreen else TextDimmed,
                        fontWeight = if (isPeak) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                peakLabel to "Peak",
                "$avg min" to "Average",
                "$total min" to "Total"
            ).forEach { (value, label) ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CardBg2)
                        .padding(10.dp)
                ) {
                    Text(
                        text = value,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SpotifyGreen
                    )
                    Text(
                        text = label.uppercase(),
                        fontSize = 10.sp,
                        color = TextDimmed,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}