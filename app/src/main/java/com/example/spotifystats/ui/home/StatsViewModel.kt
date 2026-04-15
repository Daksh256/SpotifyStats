package com.example.spotifystats.ui.home

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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

class StatsViewModel : ViewModel() {

    private val service = Retrofit.Builder()
        .baseUrl("https://api.spotify.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(SpotifyApiService::class.java)

    private val _artists = MutableStateFlow<List<Artist>>(emptyList())
    val artists = _artists.asStateFlow()

    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks = _tracks.asStateFlow()

    private val _topGenres = MutableStateFlow<List<String>>(emptyList())
    val topGenres = _topGenres.asStateFlow()

    fun fetchTopArtists(accessToken: String) {
        viewModelScope.launch {
            try {
                val response = service.getTopArtists(
                    token = "Bearer $accessToken",
                    timeRange = "short_term"
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

    fun fetchTopTracks(accessToken: String) {
        viewModelScope.launch {
            try {
                val response = service.getTopTracks(
                    token = "Bearer $accessToken",
                    timeRange = "short_term"
                )
                _tracks.value = response.items
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}