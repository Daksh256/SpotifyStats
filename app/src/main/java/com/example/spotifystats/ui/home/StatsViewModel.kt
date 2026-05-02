package com.example.spotifystats.ui.home

import android.R.attr.filter
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotifystats.api.LastFmApiService
import com.example.spotifystats.api.SpotifyApiService
import com.example.spotifystats.data.Artist
import com.example.spotifystats.data.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.collections.emptyList
import com.example.spotifystats.BuildConfig
import com.example.spotifystats.api.SpotifyAuthService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import android.util.Base64
import com.example.spotifystats.api.SupabaseApiService
import com.example.spotifystats.data.Album
import com.example.spotifystats.data.UserProfile
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from

class StatsViewModel : ViewModel() {

    private val service = Retrofit.Builder()
        .baseUrl("https://api.spotify.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(SpotifyApiService::class.java)

    private val spotifyAuthService = Retrofit.Builder()
        .baseUrl("https://accounts.spotify.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(SpotifyAuthService::class.java)
    private val _artists = MutableStateFlow<List<Artist>>(emptyList())
    val artists = _artists.asStateFlow()

    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks = _tracks.asStateFlow()

    private val _topGenres = MutableStateFlow<List<String>>(emptyList())
    val topGenres = _topGenres.asStateFlow()

    private val _recentlyPlayed = MutableStateFlow<List<Track>>(emptyList())
    val recentlyPlayed = _recentlyPlayed.asStateFlow()

    private val _currentlyPlaying = MutableStateFlow<Track?>(null)
    val currentlyPlaying = _currentlyPlaying.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _selectedTimeRange = MutableStateFlow("short_term")
    val selectedTimeRange = _selectedTimeRange.asStateFlow()

    private val _minutesThisMonth = MutableStateFlow<Int?>(null)
    val minutesThisMonth = _minutesThisMonth.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _selectedArtist = MutableStateFlow<Artist?>(null)
    val selectedArtist = _selectedArtist.asStateFlow()

    private val _selectedTrack = MutableStateFlow<Track?>(null)
    val selectedTrack = _selectedTrack.asStateFlow()

    fun selectArtist(artist: Artist) {
        _selectedArtist.value = artist
    }

    fun selectTrack(track: Track) {
        _selectedTrack.value = track
    }

    private val _artistDetailGenres = MutableStateFlow<List<String>>(emptyList())
    val artistDetailGenres = _artistDetailGenres.asStateFlow()

    private val _selectedAlbum = MutableStateFlow<Album?>(null)
    val selectedAlbum = _selectedAlbum.asStateFlow()

    private val _trendData = MutableStateFlow<Map<String, Int>>(emptyMap())
    val trendData = _trendData.asStateFlow()

    private val _trendRange = MutableStateFlow("7d")
    val trendRange = _trendRange.asStateFlow()

    fun setTrendRange(range: String, userId: String){
        _trendRange.value = range
        fetchTrendData(userId,range)
    }
    fun selectAlbum(album: Album) {
        _selectedAlbum.value = album
    }

    fun fetchTrendData(userId: String, range: String = "7d") {
        viewModelScope.launch {
            try {
                val url = "${BuildConfig.SUPABASE_URL}/rest/v1/streams" +
                        "?user_id=eq.$userId" +
                        "&select=duration_ms,played_at"

                val retrofit = Retrofit.Builder()
                    .baseUrl(BuildConfig.SUPABASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(SupabaseApiService::class.java)

                val rows = retrofit.getStreams(
                    url = url,
                    apiKey = BuildConfig.SUPABASE_ANON_KEY,
                    auth = "Bearer ${BuildConfig.SUPABASE_ANON_KEY}"
                )

                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")

                val result: Map<String, Int> = when (range) {
                    "7d" -> {
                        val days = (6 downTo 0).map { daysAgo ->
                            val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                            cal.add(java.util.Calendar.DAY_OF_YEAR, -daysAgo)
                            val dayKey = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
                                .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
                                .format(cal.time)
                            val dateKey = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
                                .format(cal.time)
                            dayKey to dateKey
                        }
                        days.associate { (dayKey, dateKey) ->
                            val mins = rows.filter { row ->
                                row.played_at.take(10) == dateKey
                            }.sumOf { it.duration_ms ?: 0L }
                            dayKey to (mins / 60000).toInt()
                        }
                    }
                    "30d" -> {
                        val weeks = listOf("W1", "W2", "W3", "W4")
                        val now = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                        val currentMonth = now.get(java.util.Calendar.MONTH) + 1
                        val currentYear = now.get(java.util.Calendar.YEAR)
                        weeks.associateWith { week ->
                            val weekNum = week.removePrefix("W").toInt()
                            val mins = rows.filter { row ->
                                try {
                                    val date = sdf.parse(row.played_at.take(19)) ?: return@filter false
                                    val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                                    cal.time = date
                                    val rowMonth = cal.get(java.util.Calendar.MONTH) + 1
                                    val rowYear = cal.get(java.util.Calendar.YEAR)
                                    val dayOfMonth = cal.get(java.util.Calendar.DAY_OF_MONTH)
                                    val rowWeek = ((dayOfMonth - 1) / 7) + 1
                                    rowMonth == currentMonth && rowYear == currentYear && rowWeek == weekNum
                                } catch (e: Exception) { false }
                            }.sumOf { it.duration_ms ?: 0L }
                            (mins / 60000).toInt()
                        }
                    }
                    "dow" -> {
                        val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                        dayNames.associateWith { dayName ->
                            val mins = rows.filter { row ->
                                try {
                                    val date = sdf.parse(row.played_at.take(19)) ?: return@filter false
                                    val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                                    cal.time = date
                                    val rowDay = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
                                        .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
                                        .format(cal.time)
                                    rowDay == dayName
                                } catch (e: Exception) { false }
                            }.sumOf { it.duration_ms ?: 0L }
                            (mins / 60000).toInt()
                        }
                    }
                    else -> emptyMap()
                }

                _trendData.value = result
            } catch (e: Exception) {
                Log.e("SUPABASE", "Failed to fetch trend: ${e.message}")
            }
        }
    }
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile = _userProfile.asStateFlow()

    fun fetchUserProfile(accessToken: String) {
        viewModelScope.launch {
            try {
                val token = "Bearer $accessToken"

                val profile = service.getCurrentUserProfile(token)

                _userProfile.value = profile
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun logout(context: android.content.Context) {
        val sharedPreferences = context.getSharedPreferences("SpotifyStatsPrefs", android.content.Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
    }
    fun fetchTopArtists(accessToken: String, timeRange: String ) {
        viewModelScope.launch {
            try {
                val response = service.getTopArtists(
                    token = "Bearer $accessToken",
                    timeRange = timeRange
                )

                android.util.Log.d("GENRE_DEBUG", "Raw JSON: $response")

                _artists.value = response.items

                val lastFmRetrofit = Retrofit.Builder()
                    .baseUrl("https://ws.audioscrobbler.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()



                val lastFmService = lastFmRetrofit.create(LastFmApiService::class.java)

                val deferredTags = response.items.take(50).map { artist ->
                    async {
                        try {
                            val tagResponse = lastFmService.getArtistTags(
                                artistName = artist.name,
                                apiKey = BuildConfig.lastFmApiKey
                            )
                            tagResponse.topTags?.tags?.map { it.name }?.take(5) ?: emptyList()
                        } catch (e: Exception) {
                            emptyList<String>()
                        }
                    }
                }

                val allGenres = deferredTags.awaitAll().flatten()
                val calculatedGenres = allGenres
                    .groupingBy { it }.eachCount()
                    .entries.sortedByDescending { it.value }
                    .take(5)
                    .map { it.key }

                _topGenres.value = calculatedGenres
                android.util.Log.d("LAST_FM_TEST", "Successfully calculated genres from Last.fm: $calculatedGenres")

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchTopTracks(accessToken: String, timeRange: String) {
        viewModelScope.launch {
            try {
                val response = service.getTopTracks(
                    token = "Bearer $accessToken",
                    timeRange = timeRange
                )
                _tracks.value = response.items
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchRecentlyPlayed(accessToken: String){
        viewModelScope.launch {
            try {
                val response = service.getRecentlyPlayed(
                    token = "Bearer $accessToken"
                )
                val tracksOnly = response.items.map { it.track }
                _recentlyPlayed.value = tracksOnly
            } catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    fun fetchCurrentlyPlaying(accessToken: String) {
        viewModelScope.launch {
            try {
                val response = service.getCurrentlyPlaying("Bearer $accessToken")
                android.util.Log.d("NOW_PLAYING", "Code: ${response.code()}")
                if (response.code() == 204) {
                    try {
                        val historyResponse = service.getRecentlyPlayed(
                            token = "Bearer $accessToken",
                            limit = 1
                        )
                        val lastSong = historyResponse.items.firstOrNull()?.track

                        _currentlyPlaying.value = lastSong
                        _isPlaying.value = false

                        android.util.Log.d("NOW_PLAYING", "Music stopped. Showing last played: ${lastSong?.name}")

                    } catch (e: Exception) {
                        _currentlyPlaying.value = null
                        _isPlaying.value = false
                    }

                    return@launch
                }

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!

                    if (body.track != null) {
                        _currentlyPlaying.value = body.track
                        _isPlaying.value = body.isPlaying
                        android.util.Log.d("NOW_PLAYING", "Currently Playing: ${body.track.name}")
                    }
                }

            } catch (e: Exception) {
                android.util.Log.e("NOW_PLAYING", "Error: ${e.message}")
                _currentlyPlaying.value = null
                _isPlaying.value = false
            }
        }
    }
    suspend fun refreshSpotifyToken(refreshToken: String): String? {
        return try {
            val authString = "${BuildConfig.SPOTIFY_CLIENT_ID}:${BuildConfig.SPOTIFY_CLIENT_SECRET}"
            val base64Auth = Base64.encodeToString(authString.toByteArray(), Base64.NO_WRAP)
            val headerMap = "Basic $base64Auth"

            val response = spotifyAuthService.refreshAccessToken(
                authorization = headerMap,
                grantType = "refresh_token",
                refreshToken = refreshToken
            )

            if (response.isSuccessful && response.body() != null) {
                val newAccessToken = response.body()!!.accessToken
                Log.d("SPOTIFY_AUTH", "Successfully refreshed token! New Token: $newAccessToken")
                return newAccessToken
            } else {
                Log.e("SPOTIFY_AUTH", "Refresh failed: ${response.errorBody()?.string()}")
                return null
            }
        } catch (e: Exception) {
            Log.e("SPOTIFY_AUTH", "Network error during refresh", e)
            return null
        }
    }
    fun updateTimeRange(newRange: String, accessToken: String) {
        _selectedTimeRange.value = newRange
        fetchTopArtists(accessToken, newRange)
        fetchTopTracks(accessToken, newRange)
    }

    fun fetchGenresForDetailScreen(artistName: String) {
        _artistDetailGenres.value = emptyList()

        viewModelScope.launch {
            try {
                val lastFmService = Retrofit.Builder()
                    .baseUrl("https://ws.audioscrobbler.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(LastFmApiService::class.java)

                val tagResponse = lastFmService.getArtistTags(
                    artistName = artistName,
                    apiKey = BuildConfig.lastFmApiKey
                )

                val tags = tagResponse.topTags?.tags?.map { it.name } ?: emptyList()
                _artistDetailGenres.value = tags

            } catch (e: Exception) {
                _artistDetailGenres.value = emptyList()
            }
        }
    }
    fun fetchMinutesThisMonth(userId: String) {
        viewModelScope.launch {
            try {
                val url = "${BuildConfig.SUPABASE_URL}/rest/v1/streams" +
                        "?user_id=eq.$userId" +
                        "&select=duration_ms,played_at"

                val retrofit = Retrofit.Builder()
                    .baseUrl(BuildConfig.SUPABASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(SupabaseApiService::class.java)

                val rows = retrofit.getStreams(
                    url = url,
                    apiKey = BuildConfig.SUPABASE_ANON_KEY,
                    auth = "Bearer ${BuildConfig.SUPABASE_ANON_KEY}"
                )

                Log.d("SUPABASE", "Total rows fetched: ${rows.size}")

                val now = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                val currentMonth = now.get(java.util.Calendar.MONTH) + 1
                val currentYear = now.get(java.util.Calendar.YEAR)

                Log.d("SUPABASE", "Filtering for month=$currentMonth year=$currentYear")

                val filtered = rows.filter { row ->
                    try {
                        val cleaned = row.played_at
                            .replace("Z", "+00:00")
                            .take(19)
                        val sdf = java.text.SimpleDateFormat(
                            "yyyy-MM-dd'T'HH:mm:ss",
                            java.util.Locale.getDefault()
                        )
                        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                        val date = sdf.parse(cleaned) ?: return@filter false

                        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                        cal.time = date

                        val rowMonth = cal.get(java.util.Calendar.MONTH) + 1
                        val rowYear = cal.get(java.util.Calendar.YEAR)

                        Log.d("SUPABASE", "Row: ${row.played_at} → month=$rowMonth year=$rowYear")

                        rowMonth == currentMonth && rowYear == currentYear
                    } catch (e: Exception) {
                        Log.e("SUPABASE", "Date parse failed for: ${row.played_at}")
                        false
                    }
                }

                Log.d("SUPABASE", "Filtered rows this month: ${filtered.size}")

                val totalMs = filtered.sumOf { it.duration_ms ?: 0L }
                _minutesThisMonth.value = (totalMs / 60000).toInt()

                Log.d("SUPABASE", "Minutes this month: ${_minutesThisMonth.value}")

            } catch (e: Exception) {
                Log.e("SUPABASE", "Failed to fetch minutes: ${e.message}")
            }
        }
    }

    fun refreshAll(accessToken: String, userId: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            fetchCurrentlyPlaying(accessToken)
            fetchTopArtists(accessToken, _selectedTimeRange.value)
            fetchTopTracks(accessToken, _selectedTimeRange.value)
            fetchRecentlyPlayed(accessToken)
            fetchMinutesThisMonth(userId)
            _isRefreshing.value = false
            fetchTrendData(userId, _trendRange.value) // add this
            _isRefreshing.value = false
        }
    }
}
