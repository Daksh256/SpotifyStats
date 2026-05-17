package com.example.spotifystats.ui.recap

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotifystats.BuildConfig
import com.example.spotifystats.api.SpotifyApiService
import com.example.spotifystats.api.SupabaseApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RecapViewModel : ViewModel() {

    data class RecapArtist(
        val artistId: String,
        val artistName: String,
        val imageUrl: String,
        val playCount: Int
    )

    data class RecapTrack(
        val trackId: String,
        val trackName: String,
        val artistName: String,
        val albumArt: String,
        val playCount: Int
    )

    private val _topTracks = MutableStateFlow<List<RecapTrack>>(emptyList())
    val topTracks = _topTracks.asStateFlow()

    private val _topArtists = MutableStateFlow<List<RecapArtist>>(emptyList())
    val topArtists = _topArtists.asStateFlow()
    private val _recapMonth = MutableStateFlow(currentMonthName())
    val recapMonth = _recapMonth.asStateFlow()

    private val _recapYear = MutableStateFlow(currentYear())
    val recapYear = _recapYear.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _totalMinutes = MutableStateFlow(0)
    val totalMinutes = _totalMinutes.asStateFlow()

    private val _totalStreams = MutableStateFlow(0)
    val totalStreams = _totalStreams.asStateFlow()

    private val _totalHours = MutableStateFlow(0f)
    val totalHours = _totalHours.asStateFlow()

    private val _topDay = MutableStateFlow("--")
    val topDay = _topDay.asStateFlow()

    private val _totalArtists = MutableStateFlow(0)
    val totalArtists = _totalArtists.asStateFlow()

    private fun currentMonthName(): String {
        val cal = java.util.Calendar.getInstance()
        return java.text.SimpleDateFormat("MMMM", java.util.Locale.getDefault())
            .format(cal.time).uppercase()
    }

    private fun currentMonth(): Int =
        java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1

    private fun currentYear(): Int =
        java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)

    fun fetchRecap(userId: String, accessToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val spotifyService = Retrofit.Builder()
                    .baseUrl("https://api.spotify.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(SpotifyApiService::class.java)

                val url = "${BuildConfig.SUPABASE_URL}/rest/v1/streams" +
                        "?user_id=eq.$userId" +
                        "&select=duration_ms,played_at,artist_id,track_id"

                val rows = Retrofit.Builder()
                    .baseUrl(BuildConfig.SUPABASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(SupabaseApiService::class.java)
                    .getStreams(
                        url = url,
                        apiKey = BuildConfig.SUPABASE_ANON_KEY,
                        auth = "Bearer ${BuildConfig.SUPABASE_ANON_KEY}"
                    )

                val sdf = java.text.SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss",
                    java.util.Locale.getDefault()
                ).apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }

                val now = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                val currentMonth = now.get(java.util.Calendar.MONTH) + 1
                val currentYear = now.get(java.util.Calendar.YEAR)

                val filtered = rows.filter { row ->
                    try {
                        val date = sdf.parse(row.played_at.take(19)) ?: return@filter false
                        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                        cal.time = date
                        cal.get(java.util.Calendar.MONTH) + 1 == currentMonth &&
                                cal.get(java.util.Calendar.YEAR) == currentYear
                    } catch (e: Exception) { false }
                }

                val totalMs = filtered.sumOf { it.duration_ms ?: 0L }
                _totalMinutes.value = (totalMs / 60000).toInt()
                _totalHours.value = totalMs / 3600000f
                _totalStreams.value = filtered.size

                val dayFormat = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
                    .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }

                val minutesByDay = filtered
                    .groupBy { row ->
                        try {
                            val date = sdf.parse(row.played_at.take(19)) ?: return@groupBy "?"
                            dayFormat.format(date)
                        } catch (e: Exception) { "?" }
                    }
                    .mapValues { (_, rows) ->
                        (rows.sumOf { it.duration_ms ?: 0L } / 60000).toInt()
                    }

                _topDay.value = minutesByDay.maxByOrNull { it.value }?.key ?: "--"
                _totalArtists.value = filtered.mapNotNull { it.artist_id }.distinct().size

                val artistCounts = filtered
                    .filter { it.artist_id != null }
                    .groupBy { it.artist_id!! }
                    .mapValues { (_, groupedRows) -> groupedRows.size }
                    .entries
                    .sortedByDescending { it.value }
                    .take(5)


                val enriched = artistCounts.mapNotNull { (artistId, playCount) ->
                    try {
                        val response = spotifyService.getArtist(
                            token = "Bearer $accessToken",
                            artistId = artistId
                        )
                        RecapArtist(
                            artistId = artistId,
                            artistName = response.name,
                            imageUrl = response.images.firstOrNull()?.url ?: "",
                            playCount = playCount
                        )
                    } catch (e: Exception) {
                        Log.e("RECAP", "Failed to enrich artist $artistId: ${e.message}")
                        null
                    }
                }

                val trackCounts = filtered
                    .groupBy { it.track_id }
                    .mapValues { (_, rows) -> rows.size }
                    .entries
                    .sortedByDescending { it.value }
                    .take(5)

                val enrichedTracks = trackCounts.mapNotNull { (trackId, playCount) ->
                    try {
                        val response = spotifyService.getTrack(
                            token = "Bearer $accessToken",
                            trackId = trackId
                        )
                        RecapTrack(
                            trackId = trackId,
                            trackName = response.name,
                            artistName = response.artists.firstOrNull()?.name ?: "",
                            albumArt = response.album.images.firstOrNull()?.url ?: "",
                            playCount = playCount
                        )
                    } catch (e: Exception) {
                        Log.e("RECAP", "Failed to enrich track $trackId: ${e.message}")
                        null
                    }
                }

                _topTracks.value = enrichedTracks

                _topArtists.value = enriched

            } catch (e: Exception) {
                Log.e("RECAP", "Failed: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}