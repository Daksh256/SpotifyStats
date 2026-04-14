package com.example.spotifystats.data

import com.google.gson.annotations.SerializedName

data class TopArtistsResponse(
    val items: List<Artist>
)

data class Artist(
    val id: String,
    val name: String,
    val images: List<SpotifyImage>,
)


data class SpotifyImage(
    val url: String,
    val height: Int,
    val width: Int
)

