package com.example.fantasycricketapp.data.data.remote



import com.example.fantasycricketapp.data.model.api.ApiMatch
import com.example.fantasycricketapp.data.model.api.ApiMatchInfo
import com.example.fantasycricketapp.data.model.api.ApiMatchSquad
import com.example.fantasycricketapp.data.model.api.CricApiListResponse
import com.example.fantasycricketapp.data.model.api.CricApiObjectResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * CricApiService — Retrofit interface defining all API endpoints.
 *
 * Retrofit reads these annotations and generates the actual HTTP
 * call implementation automatically at runtime.
 *
 * All functions are 'suspend' — they run on a coroutine (background thread)
 * and won't block the main UI thread.
 *
 * CricAPI Documentation: https://cricapi.com/how-to-use/
 */
interface CricApiService {

    /**
     * GET /currentMatches
     * Returns live and very recently completed matches.
     * This is the primary endpoint for the Dashboard match list.
     *
     * @param apiKey  Your CricAPI key
     * @param offset  Pagination offset (0 = first page)
     */
    @GET("currentMatches")
    suspend fun getCurrentMatches(
        @Query("apikey") apiKey: String,
        @Query("offset") offset: Int = 0
    ): Response<CricApiListResponse<ApiMatch>>

    /**
     * GET /matches
     * Returns all upcoming + recent matches (broader than currentMatches).
     * Used as fallback or for "Upcoming" tab filtering.
     *
     * @param apiKey  Your CricAPI key
     * @param offset  Pagination offset
     */
    @GET("matches")
    suspend fun getAllMatches(
        @Query("apikey") apiKey: String,
        @Query("offset") offset: Int = 0
    ): Response<CricApiListResponse<ApiMatch>>

    /**
     * GET /match_info
     * Returns detailed info for one match including scorecard.
     * Used for points calculation after match ends.
     *
     * @param apiKey   Your CricAPI key
     * @param matchId  The match's unique ID (from currentMatches response)
     */
    @GET("match_info")
    suspend fun getMatchInfo(
        @Query("apikey") apiKey: String,
        @Query("id")     matchId: String
    ): Response<CricApiObjectResponse<ApiMatchInfo>>

    /**
     * GET /match_squad
     * Returns both teams' squads for a specific match.
     * Used on the Team Selection screen to show real players.
     *
     * @param apiKey   Your CricAPI key
     * @param matchId  The match's unique ID
     */
    @GET("match_squad")
    suspend fun getMatchSquad(
        @Query("apikey") apiKey: String,
        @Query("id")     matchId: String
    ): Response<CricApiObjectResponse<ApiMatchSquad>>
}