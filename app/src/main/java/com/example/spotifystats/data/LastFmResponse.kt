package com.example.spotifystats.data

import com.google.gson.annotations.SerializedName

data class LastFmTagsResponse(
    @SerializedName("toptags")
    val topTags: TopTags?
)

data class TopTags(
    @SerializedName("tag")
    val tags: List<LastFmTag>?
)

data class LastFmTag(
    @SerializedName("name")
    val name: String
)