package com.example.spotifystats.data

data class TopTracksResponse(
    val items: List<Track>
)

data class Track(
    val id: String,
    val name: String,
    val duration_ms: Long,
    val album: Album,
    val artists: List<TrackArtist>
)

data class Album(
    val id: String,
    val name: String,
    val images: List<SpotifyImage>,
    val release_date: String = "",
    val artists: List<Artist> = emptyList()
)

data class TrackArtist(
    val id: String,
    val name: String
)
