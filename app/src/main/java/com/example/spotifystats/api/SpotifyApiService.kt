package com.example.spotifystats.api

import com.example.spotifystats.data.TopArtistsResponse
import com.example.spotifystats.data.TopTracksResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface SpotifyApiService {
    @GET("v1/me/top/artists")
    suspend fun getTopArtists(
        @Header("Authorization") token: String,
        @Query("time_range") timeRange: String,
        @Query("limit") limit: Int = 50
    ): TopArtistsResponse

    @GET(value = "v1/me/top/tracks")
    suspend fun getTopTracks(
        @Header("Authorization") token: String,
        @Query("time_range") timeRange: String,
        @Query("limit") limit: Int = 50
    ): TopTracksResponse
}
