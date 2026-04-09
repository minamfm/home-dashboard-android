package com.morgans.dashboard.data.repository

import com.morgans.dashboard.data.api.DashboardApi
import com.morgans.dashboard.data.auth.TokenManager
import com.morgans.dashboard.data.model.LoginRequest
import com.morgans.dashboard.data.model.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: DashboardApi,
    private val tokenManager: TokenManager,
) {
    val isLoggedIn = tokenManager.isLoggedIn
    val userRole = tokenManager.userRole

    suspend fun login(username: String, password: String): User {
        val response = api.login(LoginRequest(username, password))
        tokenManager.saveSession(
            token = response.token,
            userId = response.user.id,
            username = response.user.username,
            role = response.user.role,
        )
        return response.user
    }

    suspend fun verifySession(): Boolean {
        return try {
            val result = api.verifyToken()
            result.valid
        } catch (_: Exception) {
            false
        }
    }

    suspend fun getMe(): User = api.getMe()

    suspend fun logout() {
        tokenManager.clearSession()
    }

    suspend fun isAdmin(): Boolean = tokenManager.isAdmin()
}
