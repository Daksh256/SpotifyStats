package com.example.spotifystats.api

import com.example.spotifystats.data.CurrentlyPlayingResponse
import com.example.spotifystats.data.RecentlyPlayedResponse
import com.example.spotifystats.data.TopArtistsResponse
import com.example.spotifystats.data.TopTracksResponse
import retrofit2.Response
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

    @GET("v1/me/player/recently-played")
    suspend fun getRecentlyPlayed(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 50
    ): RecentlyPlayedResponse

    @GET("v1/me/player/currently-playing")
    suspend fun getCurrentlyPlaying(
        @Header("Authorization") token: String
    ): Response<CurrentlyPlayingResponse>

    @GET("v1/me")
    suspend fun getCurrentUserProfile(
        @Header("Authorization") token: String
    ): com.example.spotifystats.data.UserProfile

    @GET("v1/me")
    suspend fun getCurrentUser(
        @Header("Authorization") token: String
    ): Response<com.example.spotifystats.data.UserProfile>
}
