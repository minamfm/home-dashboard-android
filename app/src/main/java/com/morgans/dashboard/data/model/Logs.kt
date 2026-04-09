package com.morgans.dashboard.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ActivityResponse(
    val logs: List<ActivityLog> = emptyList(),
    val total: Int = 0,
)

@Serializable
data class ActivityLog(
    val id: Int,
    val action: String,
    val category: String = "",
    val username: String = "",
    val details: String? = null,
    val timestamp: String = "",
)

@Serializable
data class ActivityStatsResponse(
    val actionsToday: Int = 0,
    val mostActiveUser: String? = null,
    val activeUsers: Int = 0,
    val topCategory: String? = null,
)
