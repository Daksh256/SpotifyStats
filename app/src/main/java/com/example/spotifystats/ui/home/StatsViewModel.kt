package com.example.spotifystats.ui.home

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
import com.example.spotifystats.data.Album
import com.example.spotifystats.data.UserProfile

class StatsViewModel : ViewModel() {

    private val service = Retrofit.Builder()
        .baseUrl("https://api.spotify.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(SpotifyApiService::class.java)

    private val spotifyAuthService = Retrofit.Builder()
        .baseUrl("https://accounts.spotify.com/") // Notice the different URL
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

    fun selectAlbum(album: Album) {
        _selectedAlbum.value = album
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
}