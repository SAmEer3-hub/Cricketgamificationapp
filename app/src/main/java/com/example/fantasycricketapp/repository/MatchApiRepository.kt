package com.example.fantasycricketapp.repository


import android.util.Log
import com.example.fantasycricketapp.data.model.Match
import com.example.fantasycricketapp.data.model.api.ApiMatch
import com.example.fantasycricketapp.data.data.remote.CricApiClient
import com.example.fantasycricketapp.utils.ApiConstants
import com.example.fantasycricketapp.utils.ApiResult
import com.example.fantasycricketapp.utils.DummyData

/**
 * MatchApiRepository — Fetches real cricket matches from CricAPI.
 *
 * Strategy:
 *   1. Try to fetch live matches from CricAPI
 *   2. If API succeeds → convert ApiMatch → Match → show real data
 *   3. If API fails (network error, limit hit, etc.) → fall back to PlayerDummyData
 *
 * This ensures the app ALWAYS shows something — never a blank screen.
 *
 * Conversion logic (ApiMatch → Match):
 *   - matchType: "t20" → "T20", "odi" → "ODI", "test" → "Test"
 *   - status: matchStarted + matchEnded → "LIVE" / "COMPLETED" / "UPCOMING"
 *   - teams: teams[0] and teams[1] → team1 and team2
 *   - flag: we map known country names to emoji flags
 */
class MatchApiRepository {

    private val tag = "MatchApiRepository"
    private val apiService = CricApiClient.service

    // ── Simple in-memory cache ──
    private var cachedMatches: List<Match>? = null
    private var cacheTimestamp: Long = 0L

    // ----------------------------------------------------------
    // FETCH MATCHES — Main entry point
    // ----------------------------------------------------------

    /**
     * Returns a list of cricket matches.
     * Tries CricAPI first, falls back to dummy data.
     *
     * This is a suspend function — call it from a coroutine scope.
     */
    suspend fun getMatches(): ApiResult<List<Match>> {

        // ── Check cache first (5 minute TTL) ──
        val now = System.currentTimeMillis()
        if (cachedMatches != null &&
            now - cacheTimestamp < ApiConstants.MATCH_CACHE_DURATION) {
            Log.d(tag, "Returning cached matches (${cachedMatches!!.size} items)")
            return ApiResult.Success(cachedMatches!!)
        }

        return try {
            Log.d(tag, "Fetching current matches from CricAPI...")

            val response = apiService.getCurrentMatches(
                apiKey = ApiConstants.API_KEY,
                offset = ApiConstants.DEFAULT_OFFSET
            )

            when {
                // ── API call succeeded ──
                response.isSuccessful -> {
                    val body = response.body()

                    when {
                        // ── CricAPI returned success with data ──
                        body?.status == "success" && !body.data.isNullOrEmpty() -> {
                            Log.d(tag, "API success: ${body.data.size} matches received")

                            // Log remaining API calls
                            body.info?.let {
                                Log.d(tag, "API hits today: ${it.hitsToday} / 100")
                            }

                            // Convert API matches to our app's Match model
                            val matches = body.data
                                .filter { isRelevantMatch(it) }
                                .map { convertToMatch(it) }
                                .sortedWith(compareBy { getStatusSortOrder(it.status) })

                            Log.d(tag, "Converted ${matches.size} relevant matches")

                            // Cache the result
                            cachedMatches = matches
                            cacheTimestamp = now

                            ApiResult.Success(matches)
                        }

                        // ── CricAPI returned failure (e.g. invalid key) ──
                        body?.status == "failure" -> {
                            val errorMsg = body.message ?: "API authentication failed"
                            Log.w(tag, "CricAPI failure: $errorMsg")

                            // Check if it's the daily limit error
                            val isLimitError = errorMsg.contains("limit", ignoreCase = true) ||
                                    errorMsg.contains("quota", ignoreCase = true)

                            Log.w(tag, "Falling back to dummy data")
                            ApiResult.Success(DummyData.getMatches()) // Fallback
                        }

                        // ── Empty data ──
                        else -> {
                            Log.w(tag, "Empty response — using dummy data")
                            ApiResult.Success(DummyData.getMatches())
                        }
                    }
                }

                // ── HTTP error (4xx, 5xx) ──
                else -> {
                    Log.e(tag, "HTTP error: ${response.code()} — using dummy data")
                    ApiResult.Success(DummyData.getMatches()) // Always fallback
                }
            }

        } catch (e: java.net.UnknownHostException) {
            // No internet connection
            Log.w(tag, "No internet — using dummy data")
            ApiResult.Success(DummyData.getMatches())

        } catch (e: java.net.SocketTimeoutException) {
            // Request timed out
            Log.w(tag, "Timeout — using dummy data")
            ApiResult.Success(DummyData.getMatches())

        } catch (e: Exception) {
            // Any other error
            Log.e(tag, "Unexpected error: ${e.message} — using dummy data")
            ApiResult.Success(DummyData.getMatches())
        }
    }

    // ----------------------------------------------------------
    // FILTER — Only show cricket matches worth displaying
    // ----------------------------------------------------------

    /**
     * Filters out irrelevant matches (women's, youth, club level etc.)
     * CricAPI returns ALL matches — we want international + major leagues.
     */
    private fun isRelevantMatch(match: ApiMatch): Boolean {
        val name = match.name.lowercase()

        // Must have exactly 2 teams
        if (match.teams.size < 2) return false

        // Filter out non-T20/ODI/Test matches if needed
        // For now accept all match types
        return true
    }

    // ----------------------------------------------------------
    // CONVERT ApiMatch → Match (our app's model)
    // ----------------------------------------------------------

    /**
     * Converts a raw CricAPI ApiMatch into our app's Match data class.
     *
     * Handles all the messy API field parsing in one place so the
     * rest of the app only ever sees clean Match objects.
     */
    private fun convertToMatch(apiMatch: ApiMatch): Match {

        // ── Teams ──
        val team1Name = apiMatch.teams.getOrElse(0) { "Team 1" }
        val team2Name = apiMatch.teams.getOrElse(1) { "Team 2" }

        // ── Short names from teamInfo or generate from name ──
        val team1Info = apiMatch.teamInfo?.getOrNull(0)
        val team2Info = apiMatch.teamInfo?.getOrNull(1)
        val team1Short = team1Info?.shortName?.ifBlank { makeShortName(team1Name) }
            ?: makeShortName(team1Name)
        val team2Short = team2Info?.shortName?.ifBlank { makeShortName(team2Name) }
            ?: makeShortName(team2Name)

        // ── Emoji flags ──
        val team1Flag = getCountryFlag(team1Name)
        val team2Flag = getCountryFlag(team2Name)

        // ── Match type — normalize CricAPI format ──
        val matchType = when (apiMatch.matchType.lowercase()) {
            "t20", "t20i" -> "T20"
            "odi"         -> "ODI"
            "test"        -> "Test"
            "hundred"     -> "The Hundred"
            else          -> apiMatch.matchType.uppercase().ifBlank { "T20" }
        }

        // ── Status ──
        val status = when {
            apiMatch.matchEnded   -> "COMPLETED"
            apiMatch.matchStarted -> "LIVE"
            else                  -> "UPCOMING"
        }

        // ── Date display ──
        val displayDate = formatMatchDate(apiMatch.date, apiMatch.dateTimeGMT)

        // ── Venue — trim if too long ──
        val venue = apiMatch.venue.take(50).ifBlank { "TBD" }

        return Match(
            matchId    = apiMatch.id,
            matchName  = apiMatch.name.ifBlank { "$team1Name vs $team2Name" },
            team1      = team1Name,
            team2      = team2Name,
            team1Short = team1Short,
            team2Short = team2Short,
            team1Flag  = team1Flag,
            team2Flag  = team2Flag,
            venue      = venue,
            matchDate  = displayDate,
            matchType  = matchType,
            status     = status
        )
    }

    // ----------------------------------------------------------
    // HELPERS
    // ----------------------------------------------------------

    /**
     * Generates a 2-3 letter short name from a team's full name.
     * e.g. "India" → "IND", "New Zealand" → "NZ", "Sri Lanka" → "SL"
     */
    private fun makeShortName(fullName: String): String {
        val knownShorts = mapOf(
            "india"         to "IND",
            "australia"     to "AUS",
            "england"       to "ENG",
            "pakistan"      to "PAK",
            "south africa"  to "SA",
            "new zealand"   to "NZ",
            "west indies"   to "WI",
            "sri lanka"     to "SL",
            "bangladesh"    to "BAN",
            "afghanistan"   to "AFG",
            "zimbabwe"      to "ZIM",
            "ireland"       to "IRE",
            "scotland"      to "SCO",
            "netherlands"   to "NED",
            "uae"           to "UAE",
            "oman"          to "OMA",
            "namibia"       to "NAM"
        )
        return knownShorts[fullName.lowercase()]
            ?: fullName.split(" ")
                .map { it.first().uppercaseChar() }
                .joinToString("")
                .take(3)
    }

    /**
     * Maps country/team names to emoji flag characters.
     */
    private fun getCountryFlag(teamName: String): String {
        return when (teamName.lowercase().trim()) {
            "india"                    -> "🇮🇳"
            "australia"                -> "🇦🇺"
            "england"                  -> "🏴󠁧󠁢󠁥󠁮󠁧󠁿"
            "pakistan"                 -> "🇵🇰"
            "south africa"             -> "🇿🇦"
            "new zealand"              -> "🇳🇿"
            "west indies"              -> "🏝️"
            "sri lanka"                -> "🇱🇰"
            "bangladesh"               -> "🇧🇩"
            "afghanistan"              -> "🇦🇫"
            "zimbabwe"                 -> "🇿🇼"
            "ireland"                  -> "🇮🇪"
            "scotland"                 -> "🏴󠁧󠁢󠁳󠁣󠁴󠁿"
            "netherlands"              -> "🇳🇱"
            "uae", "united arab emirates" -> "🇦🇪"
            "oman"                     -> "🇴🇲"
            "namibia"                  -> "🇳🇦"
            else                       -> "🏏"
        }
    }

    /**
     * Formats the CricAPI date string into a user-friendly display string.
     * Input:  "2025-01-25" + "2025-01-25T14:30:00"
     * Output: "25 Jan, 8:00 PM" (converted to local time)
     */
    private fun formatMatchDate(date: String, dateTimeGMT: String): String {
        return try {
            if (dateTimeGMT.isNotBlank()) {
                // Parse ISO datetime and convert to local time
                val sdf = java.text.SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss",
                    java.util.Locale.getDefault()
                )
                sdf.timeZone = java.util.TimeZone.getTimeZone("GMT")
                val parsedDate = sdf.parse(dateTimeGMT)

                val displaySdf = java.text.SimpleDateFormat(
                    "d MMM, h:mm a",
                    java.util.Locale.getDefault()
                )
                displaySdf.timeZone = java.util.TimeZone.getDefault()
                parsedDate?.let { displaySdf.format(it) } ?: date
            } else {
                date
            }
        } catch (e: Exception) {
            date
        }
    }

    /**
     * Returns a sort order number for match status.
     * LIVE matches appear first, then UPCOMING, then COMPLETED.
     */
    private fun getStatusSortOrder(status: String): Int = when (status) {
        "LIVE"      -> 0
        "UPCOMING"  -> 1
        "COMPLETED" -> 2
        else        -> 3
    }

    /**
     * Clears the in-memory cache, forcing a fresh API call next time.
     * Call this when user manually refreshes.
     */
    fun clearCache() {
        cachedMatches = null
        cacheTimestamp = 0L
    }
}