package com.morgans.dashboard.data.api

import com.morgans.dashboard.data.model.*
import kotlinx.serialization.json.JsonObject
import retrofit2.Response
import retrofit2.http.*

interface DashboardApi {

    // ── Auth ────────────────────────────────────────────────────────────
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("api/auth/me")
    suspend fun getMe(): User

    @GET("api/auth/verify")
    suspend fun verifyToken(): AuthVerifyResponse

    // ── Smart Home ──────────────────────────────────────────────────────
    @GET("api/smarthome")
    suspend fun getSmartHome(): SmartHomeResponse

    @GET("api/smarthome/hidden-rooms")
    suspend fun getHiddenRooms(): HiddenRoomsResponse

    @POST("api/smarthome/hidden-rooms")
    suspend fun setHiddenRooms(@Body rooms: HiddenRoomsResponse): Response<Unit>

    @POST("api/smarthome/service")
    suspend fun callService(@Body request: ServiceCallRequest): Response<Unit>

    @GET("api/smarthome/power-health")
    suspend fun getPowerHealth(): PowerHealthResponse

    // ── Photos ──────────────────────────────────────────────────────────
    @GET("api/photos")
    suspend fun getPhotos(): PhotosStatusResponse

    // ── Media ───────────────────────────────────────────────────────────
    @GET("api/media/libraries")
    suspend fun getLibraries(): LibrariesResponse

    @GET("api/media/library/{id}")
    suspend fun getLibraryItems(@Path("id") id: String): LibraryItemsResponse

    @GET("api/media/item/{ratingKey}")
    suspend fun getMediaDetail(@Path("ratingKey") ratingKey: String): MediaDetailResponse

    @GET("api/media/recent")
    suspend fun getRecentlyAdded(): RecentlyAddedResponse

    @GET("api/media/continue")
    suspend fun getContinueWatching(): ContinueWatchingResponse

    @POST("api/media/progress")
    suspend fun saveProgress(@Body request: ProgressRequest): Response<Unit>

    @GET("api/media/subtitles/search")
    suspend fun searchSubtitles(
        @Query("ratingKey") ratingKey: String,
        @Query("language") language: String = "en",
    ): SubtitleSearchResponse

    @POST("api/media/subtitles/download")
    suspend fun downloadSubtitle(@Body body: Map<String, String>): Response<Unit>

    @DELETE("api/media/item/{ratingKey}")
    suspend fun deleteMedia(@Path("ratingKey") ratingKey: String): Response<Unit>

    @POST("api/media/refresh")
    suspend fun refreshLibrary(): Response<Unit>

    // ── Torrents ────────────────────────────────────────────────────────
    @GET("api/torrents")
    suspend fun getTorrents(): TorrentsResponse

    @GET("api/torrents/history")
    suspend fun getTorrentHistory(): TorrentHistoryResponse

    @GET("api/torrents/search")
    suspend fun searchTorrents(
        @Query("q") query: String,
        @Query("type") type: String = "movie",
    ): TorrentSearchResponse

    @POST("api/torrents/add")
    suspend fun addTorrent(@Body request: AddTorrentRequest): Response<Unit>

    @DELETE("api/torrents/{hash}")
    suspend fun deleteTorrent(@Path("hash") hash: String): Response<Unit>

    @POST("api/torrents/pause/{hash}")
    suspend fun pauseTorrent(@Path("hash") hash: String): Response<Unit>

    @POST("api/torrents/resume/{hash}")
    suspend fun resumeTorrent(@Path("hash") hash: String): Response<Unit>

    // ── Expenses ────────────────────────────────────────────────────────
    @GET("api/expenses")
    suspend fun getExpenses(
        @Query("month") month: Int,
        @Query("year") year: Int,
        @Query("user_id") userId: Int? = null,
    ): ExpensesResponse

    @POST("api/expenses")
    suspend fun addExpense(@Body expense: Map<String, @JvmSuppressWildcards Any>): Response<Unit>

    // ── Income ──────────────────────────────────────────────────────────
    @GET("api/income")
    suspend fun getIncome(
        @Query("month") month: Int,
        @Query("year") year: Int,
    ): IncomeResponse

    // ── Logs ────────────────────────────────────────────────────────────
    @GET("api/logs/activity")
    suspend fun getActivityLogs(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
        @Query("category") category: String? = null,
        @Query("user") user: String? = null,
    ): ActivityResponse

    @GET("api/logs/activity/stats")
    suspend fun getActivityStats(): ActivityStatsResponse

    // ── Settings ────────────────────────────────────────────────────────
    @GET("api/settings")
    suspend fun getSettings(): JsonObject

    @POST("api/settings")
    suspend fun saveSettings(@Body settings: JsonObject): Response<Unit>

    // ── Users ───────────────────────────────────────────────────────────
    @GET("api/users")
    suspend fun getUsers(): List<User>

    @POST("api/users")
    suspend fun createUser(@Body user: Map<String, String>): Response<Unit>

    @DELETE("api/users/{id}")
    suspend fun deleteUser(@Path("id") id: Int): Response<Unit>

    @PATCH("api/users/{id}/role")
    suspend fun updateUserRole(
        @Path("id") id: Int,
        @Body body: Map<String, String>,
    ): Response<Unit>

    // ── AI Chat ─────────────────────────────────────────────────────────
    @POST("api/ai/chat")
    suspend fun chat(@Body request: ChatRequest): ChatResponse

    // ── Version ─────────────────────────────────────────────────────────
    @GET("api/version")
    suspend fun getVersion(): Map<String, String>
}
