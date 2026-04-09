package com.morgans.dashboard.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TorrentsResponse(
    val torrents: List<Torrent> = emptyList(),
)

@Serializable
data class Torrent(
    val hash: String,
    val name: String,
    val progress: Double = 0.0,
    val size: Long = 0,
    val downloaded: Long = 0,
    val speed: Long = 0,
    val peers: Int = 0,
    val eta: Long = 0,
    val state: String = "",
)

@Serializable
data class TorrentHistoryResponse(
    val history: List<TorrentHistoryItem> = emptyList(),
)

@Serializable
data class TorrentHistoryItem(
    val name: String,
    val completedAt: String = "",
    val size: Long = 0,
    val subtitleStatus: String? = null,
)

@Serializable
data class TorrentSearchResponse(
    val results: List<TorrentSearchResult> = emptyList(),
)

@Serializable
data class TorrentSearchResult(
    val name: String,
    val size: String = "",
    val seeders: Int = 0,
    val leechers: Int = 0,
    val magnet: String = "",
    val description: String? = null,
)

@Serializable
data class AddTorrentRequest(
    val magnet: String,
)
