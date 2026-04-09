package com.morgans.dashboard.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
)

@Serializable
data class LoginResponse(
    val token: String,
    val user: User,
)

@Serializable
data class User(
    val id: Int,
    val username: String,
    val role: String,
)

@Serializable
data class AuthVerifyResponse(
    val valid: Boolean = false,
    val user: User? = null,
)
