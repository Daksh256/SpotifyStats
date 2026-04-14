package com.example.spotifystats.api


import com.example.spotifystats.data.LastFmTagsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface LastFmApiService {
    @GET("2.0/")
    suspend fun getArtistTags(
        @Query("method") method: String = "artist.getTopTags",
        @Query("artist") artistName: String,
        @Query("api_key") apiKey: String,
        @Query("format") format: String = "json"
    ): LastFmTagsResponse
}