package com.example.spotifystats.data

import com.google.gson.annotations.SerializedName

data class CurrentlyPlayingResponse(
    @SerializedName("is_playing")
    val isPlaying: Boolean,

    @SerializedName("item")
    val track: Track?
)