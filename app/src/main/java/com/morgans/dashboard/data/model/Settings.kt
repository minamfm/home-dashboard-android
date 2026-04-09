package com.morgans.dashboard.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class SettingsResponse(
    val settings: JsonObject,
)

@Serializable
data class PhotosStatusResponse(
    val configured: Boolean = false,
    val items: List<PhotoItem> = emptyList(),
)

@Serializable
data class PhotoItem(
    val url: String,
    val fullUrl: String = "",
    val filename: String = "",
)
