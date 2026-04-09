package com.morgans.dashboard.data.repository

import com.morgans.dashboard.data.api.DashboardApi
import com.morgans.dashboard.data.model.*
import kotlinx.serialization.json.JsonObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepository @Inject constructor(
    private val api: DashboardApi,
) {
    // ── Smart Home ──────────────────────────────────────────────────
    suspend fun getSmartHome() = api.getSmartHome()
    suspend fun callService(request: ServiceCallRequest) = api.callService(request)
    suspend fun getPowerHealth() = api.getPowerHealth()
    suspend fun getHiddenRooms() = api.getHiddenRooms()
    suspend fun setHiddenRooms(rooms: List<String>) = api.setHiddenRooms(HiddenRoomsResponse(rooms))

    // ── Photos ──────────────────────────────────────────────────────
    suspend fun getPhotos() = api.getPhotos()

    // ── Media ───────────────────────────────────────────────────────
    suspend fun getLibraries() = api.getLibraries()
    suspend fun getLibraryItems(id: String) = api.getLibraryItems(id)
    suspend fun getMediaDetail(ratingKey: String) = api.getMediaDetail(ratingKey)
    suspend fun getRecentlyAdded() = api.getRecentlyAdded()
    suspend fun getContinueWatching() = api.getContinueWatching()
    suspend fun saveProgress(ratingKey: String, offset: Long) =
        api.saveProgress(ProgressRequest(ratingKey, offset))
    suspend fun searchSubtitles(ratingKey: String, language: String) =
        api.searchSubtitles(ratingKey, language)
    suspend fun downloadSubtitle(url: String, ratingKey: String) =
        api.downloadSubtitle(mapOf("url" to url, "ratingKey" to ratingKey))
    suspend fun deleteMedia(ratingKey: String) = api.deleteMedia(ratingKey)
    suspend fun refreshLibrary() = api.refreshLibrary()

    // ── Torrents ────────────────────────────────────────────────────
    suspend fun getTorrents() = api.getTorrents()
    suspend fun getTorrentHistory() = api.getTorrentHistory()
    suspend fun searchTorrents(query: String, type: String) = api.searchTorrents(query, type)
    suspend fun addTorrent(magnet: String) = api.addTorrent(AddTorrentRequest(magnet))
    suspend fun deleteTorrent(hash: String) = api.deleteTorrent(hash)
    suspend fun pauseTorrent(hash: String) = api.pauseTorrent(hash)
    suspend fun resumeTorrent(hash: String) = api.resumeTorrent(hash)

    // ── Expenses ────────────────────────────────────────────────────
    suspend fun getExpenses(month: Int, year: Int, userId: Int? = null) =
        api.getExpenses(month, year, userId)
    suspend fun getIncome(month: Int, year: Int) = api.getIncome(month, year)

    // ── Logs ────────────────────────────────────────────────────────
    suspend fun getActivityLogs(page: Int, category: String?, user: String?) =
        api.getActivityLogs(page = page, category = category, user = user)
    suspend fun getActivityStats() = api.getActivityStats()

    // ── Settings ────────────────────────────────────────────────────
    suspend fun getSettings() = api.getSettings()
    suspend fun saveSettings(settings: JsonObject) = api.saveSettings(settings)

    // ── Users ───────────────────────────────────────────────────────
    suspend fun getUsers() = api.getUsers()
    suspend fun createUser(username: String, password: String, role: String) =
        api.createUser(mapOf("username" to username, "password" to password, "role" to role))
    suspend fun deleteUser(id: Int) = api.deleteUser(id)
    suspend fun updateUserRole(id: Int, role: String) =
        api.updateUserRole(id, mapOf("role" to role))

    // ── Chat ────────────────────────────────────────────────────────
    suspend fun chat(message: String, history: List<ChatMessage>) =
        api.chat(ChatRequest(message, history))
}
