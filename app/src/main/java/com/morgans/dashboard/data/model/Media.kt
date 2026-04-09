package com.morgans.dashboard.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LibrariesResponse(
    val libraries: List<Library> = emptyList(),
)

@Serializable
data class Library(
    val key: String,
    val title: String,
    val type: String = "",
    val count: Int = 0,
)

@Serializable
data class LibraryItemsResponse(
    val items: List<MediaItem> = emptyList(),
)

@Serializable
data class MediaItem(
    val ratingKey: String,
    val title: String,
    val year: Int? = null,
    val thumb: String? = null,
    val art: String? = null,
    val summary: String? = null,
    val duration: Long? = null,
    val type: String = "",
    val addedAt: Long? = null,
    val viewOffset: Long? = null,
)

@Serializable
data class MediaDetailResponse(
    val title: String,
    val year: Int? = null,
    val summary: String? = null,
    val duration: Long? = null,
    val streamUrl: String? = null,
    val subtitles: List<Subtitle> = emptyList(),
    val thumb: String? = null,
)

@Serializable
data class Subtitle(
    val id: String? = null,
    val language: String = "",
    val codec: String = "",
    val url: String? = null,
)

@Serializable
data class RecentlyAddedResponse(
    val items: List<MediaItem> = emptyList(),
)

@Serializable
data class ContinueWatchingResponse(
    val items: List<MediaItem> = emptyList(),
)

@Serializable
data class SubtitleSearchResponse(
    val subtitles: List<SubtitleResult> = emptyList(),
)

@Serializable
data class SubtitleResult(
    val id: String,
    val language: String,
    val release: String = "",
    val url: String = "",
)

@Serializable
data class ProgressRequest(
    val ratingKey: String,
    val offset: Long,
)
