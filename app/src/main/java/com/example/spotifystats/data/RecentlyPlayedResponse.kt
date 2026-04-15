package com.example.spotifystats.data

import com.google.gson.annotations.SerializedName

data class RecentlyPlayedResponse(
    @SerializedName("items")
    val items: List<PlayHistoryObject>
)

data class PlayHistoryObject(
    @SerializedName("track")
    val track: Track,

    @SerializedName("played_at")
    val playedAt: String
)