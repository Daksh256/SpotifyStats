package com.example.spotifystats.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotifystats.api.SpotifyApiService
import com.example.spotifystats.data.Artist
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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
                    timeRange = "medium_term"
                )

                _artists.value = response.items

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}