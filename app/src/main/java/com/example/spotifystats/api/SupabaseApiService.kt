package com.example.spotifystats.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Url

data class StreamRow(
    @SerializedName("duration_ms") val duration_ms: Long?,
    @SerializedName("played_at") val played_at: String
)

interface SupabaseApiService {
    @GET
    suspend fun getStreams(
        @Url url: String,
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String
    ): List<StreamRow>
}