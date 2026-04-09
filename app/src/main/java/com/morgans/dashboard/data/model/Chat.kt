package com.morgans.dashboard.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    val message: String,
    val history: List<ChatMessage> = emptyList(),
)

@Serializable
data class ChatMessage(
    val role: String,
    val content: String,
)

@Serializable
data class ChatResponse(
    val reply: String,
)
