package com.example.spotifystats.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotifystats.api.SpotifyApiService
import com.example.spotifystats.data.Artist
import com.example.spotifystats.data.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.collections.emptyList

class StatsViewModel : ViewModel() {

    private val _artists = MutableStateFlow<List<Artist>>(emptyList())
    val artists = _artists.asStateFlow()

    fun fetchTopArtists(accessToken: String) {
        viewModelScope.launch {
            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://api.spotify.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val service = retrofit.create(SpotifyApiService::class.java)

                val response = service.getTopArtists(
                    token = "Bearer $accessToken",
                    timeRange = "short_term"
                )

                _artists.value = response.items

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks = _tracks.asStateFlow()

    fun fetchTopTracks(accessToken: String) {
        viewModelScope.launch {
            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://api.spotify.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val service = retrofit.create(SpotifyApiService::class.java)

                val response = service.getTopTracks(
                    token = "Bearer $accessToken",
                    timeRange = "short_term"
                )

                _tracks.value = response.items

                android.util.Log.d("API_TEST", "Successfully fetched ${response.items.size} tracks!")

            } catch (e: Exception) {
                android.util.Log.e("API_TEST", "TRACKS CRASHED: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}