package com.example.fantasycricketapp.repository



import android.util.Log
import com.example.fantasycricketapp.data.model.Player
import com.example.fantasycricketapp.data.model.api.ApiPlayer
import com.example.fantasycricketapp.data.data.remote.CricApiClient
import com.example.fantasycricketapp.utils.ApiConstants
import com.example.fantasycricketapp.utils.ApiResult
import com.example.fantasycricketapp.utils.PlayerDummyData

/**
 * PlayerApiRepository — Fetches real player squads from CricAPI.
 *
 * Strategy:
 *   1. Call /match_squad with the real match ID
 *   2. Convert ApiPlayer → Player for each player in both squads
 *   3. Assign credit points based on role (since API doesn't provide these)
 *   4. If API fails → fall back to PlayerDummyData for that match
 *
 * Credit point assignment logic (since CricAPI doesn't have fantasy credits):
 *   We simulate credits based on role — in a production app these would come
 *   from a separate database or admin panel.
 *
 * Caching:
 *   Squads are cached per matchId for 30 minutes since they rarely change.
 */
class PlayerApiRepository {

    private val tag = "PlayerApiRepository"
    private val apiService = CricApiClient.service

    // Cache: matchId → list of players
    private val squadCache = mutableMapOf<String, Pair<List<Player>, Long>>()

    // ----------------------------------------------------------
    // FETCH SQUAD — Main entry point
    // ----------------------------------------------------------

    /**
     * Returns the 15-player squad for a given match.
     * Tries real API first, falls back to dummy data.
     *
     * @param matchId  The CricAPI match ID (from MatchApiRepository)
     */
    suspend fun getSquadForMatch(matchId: String): ApiResult<List<Player>> {

        // ── Check cache ──
        val cached = squadCache[matchId]
        val now = System.currentTimeMillis()
        if (cached != null && now - cached.second < ApiConstants.SQUAD_CACHE_DURATION) {
            Log.d(tag, "Returning cached squad for match $matchId")
            return ApiResult.Success(cached.first)
        }

        return try {
            Log.d(tag, "Fetching squad for match: $matchId")

            val response = apiService.getMatchSquad(
                apiKey  = ApiConstants.API_KEY,
                matchId = matchId
            )

            when {
                response.isSuccessful -> {
                    val body = response.body()

                    when {
                        body?.status == "success" && body.data != null -> {
                            val squadData = body.data
                            val allPlayers = mutableListOf<Player>()

                            // Process each team's squad
                            squadData.squads?.forEach { apiSquad ->
                                val teamShortName = makeShortName(apiSquad.teamName)
                                apiSquad.players?.forEach { apiPlayer ->
                                    val player = convertToPlayer(apiPlayer, teamShortName)
                                    allPlayers.add(player)
                                }
                            }

                            if (allPlayers.isEmpty()) {
                                Log.w(tag, "Squad empty for $matchId — using fallback")
                                return ApiResult.Success(
                                    PlayerDummyData.getPlayersForMatch(matchId)
                                )
                            }

                            // Limit to 15 players (7-8 per team)
                            val limitedPlayers = limitSquad(allPlayers)

                            Log.d(tag, "Squad loaded: ${limitedPlayers.size} players")

                            // Cache it
                            squadCache[matchId] = Pair(limitedPlayers, now)

                            ApiResult.Success(limitedPlayers)
                        }

                        body?.status == "failure" -> {
                            Log.w(tag, "Squad API failure: ${body.message}")
                            ApiResult.Success(PlayerDummyData.getPlayersForMatch(matchId))
                        }

                        else -> {
                            Log.w(tag, "Empty squad response — using fallback")
                            ApiResult.Success(PlayerDummyData.getPlayersForMatch(matchId))
                        }
                    }
                }

                else -> {
                    Log.e(tag, "HTTP error ${response.code()} — using fallback")
                    ApiResult.Success(PlayerDummyData.getPlayersForMatch(matchId))
                }
            }

        } catch (e: java.net.UnknownHostException) {
            Log.w(tag, "No internet — using dummy squad")
            ApiResult.Success(PlayerDummyData.getPlayersForMatch(matchId))
        } catch (e: Exception) {
            Log.e(tag, "Squad fetch error: ${e.message} — using fallback")
            ApiResult.Success(PlayerDummyData.getPlayersForMatch(matchId))
        }
    }

    // ----------------------------------------------------------
    // CONVERT ApiPlayer → Player
    // ----------------------------------------------------------

    /**
     * Converts a CricAPI player object into our app's Player model.
     *
     * Role mapping:
     *   "Batsman"      → "BAT"
     *   "Bowler"       → "BOWL"
     *   "All-Rounder"  → "AR"
     *   "WK-Batsman"   → "WK"
     *   "Wicket Keeper"→ "WK"
     */
    private fun convertToPlayer(apiPlayer: ApiPlayer, teamShortName: String): Player {

        // ── Role normalization ──
        val role = when {
            apiPlayer.role.contains("bat", ignoreCase = true) &&
                    apiPlayer.role.contains("wk", ignoreCase = true)     -> "WK"
            apiPlayer.role.contains("wicket", ignoreCase = true)  -> "WK"
            apiPlayer.role.contains("keeper", ignoreCase = true)  -> "WK"
            apiPlayer.role.contains("all", ignoreCase = true)     -> "AR"
            apiPlayer.role.contains("bat", ignoreCase = true)     -> "BAT"
            apiPlayer.role.contains("bowl", ignoreCase = true)    -> "BOWL"
            else                                                   -> "BAT"
        }

        // ── Credit points based on role ──
        // Real apps get these from a paid API or admin panel
        // We simulate them with a seeded random based on player name
        val credits = assignCredits(apiPlayer.name, role)

        // ── Batting style — use API data or default ──
        val battingStyle = apiPlayer.battingStyle.ifBlank {
            when (role) {
                "WK"   -> "Right-hand bat"
                else   -> "Right-hand bat"
            }
        }

        // ── Bowling style ──
        val bowlingStyle = apiPlayer.bowlingStyle.ifBlank {
            when (role) {
                "BOWL" -> "Right-arm fast"
                "AR"   -> "Right-arm medium"
                else   -> "N/A"
            }
        }

        return Player(
            playerId     = apiPlayer.id.ifBlank { apiPlayer.name.replace(" ", "_") },
            name         = apiPlayer.name,
            team         = teamShortName,
            role         = role,
            creditPoints = credits,
            battingStyle = battingStyle,
            bowlingStyle = bowlingStyle,
            isSelected   = false
        )
    }

    /**
     * Assigns a consistent credit value to a player based on their name + role.
     * Using a hash ensures the same player always gets the same credits
     * even across different API calls — creates apparent consistency.
     *
     * Credits range: 7.0 to 10.0 based on role tier
     */
    private fun assignCredits(playerName: String, role: String): Double {
        // Seed with player name hash for consistency
        val hash = Math.abs(playerName.hashCode())
        val baseRange = when (role) {
            "BAT"  -> Pair(7.5, 9.5)
            "BOWL" -> Pair(7.0, 9.5)
            "AR"   -> Pair(8.0, 9.5)
            "WK"   -> Pair(7.5, 9.0)
            else   -> Pair(7.0, 9.0)
        }
        val range = baseRange.second - baseRange.first
        val credit = baseRange.first + (hash % 10) / 10.0 * range
        // Round to nearest 0.5
        return (Math.round(credit * 2) / 2.0)
    }

    /**
     * Limits a combined squad to 15 players — max 8 from each team.
     * Ensures a good mix of roles for fantasy selection.
     */
    private fun limitSquad(allPlayers: List<Player>): List<Player> {
        // Group by team
        val byTeam = allPlayers.groupBy { it.team }
        val result = mutableListOf<Player>()

        byTeam.values.forEach { teamPlayers ->
            // Take up to 8 from each team, prioritizing role diversity
            val wk   = teamPlayers.filter { it.role == "WK" }.take(1)
            val bat  = teamPlayers.filter { it.role == "BAT" }.take(3)
            val ar   = teamPlayers.filter { it.role == "AR" }.take(2)
            val bowl = teamPlayers.filter { it.role == "BOWL" }.take(3)
            val combined = (wk + bat + ar + bowl).take(8)
            result.addAll(combined)
        }

        // If still less than 15, add more players
        if (result.size < 15) {
            val extras = allPlayers
                .filter { p -> result.none { it.playerId == p.playerId } }
                .take(15 - result.size)
            result.addAll(extras)
        }

        return result.take(15)
    }

    /**
     * Generates a short team name from full name.
     */
    private fun makeShortName(fullName: String): String {
        val knownShorts = mapOf(
            "india" to "IND", "australia" to "AUS", "england" to "ENG",
            "pakistan" to "PAK", "south africa" to "SA", "new zealand" to "NZ",
            "west indies" to "WI", "sri lanka" to "SL", "bangladesh" to "BAN",
            "afghanistan" to "AFG", "zimbabwe" to "ZIM", "ireland" to "IRE"
        )
        return knownShorts[fullName.lowercase()]
            ?: fullName.split(" ").map { it.first().uppercaseChar() }
                .joinToString("").take(3)
    }

    /**
     * Clears the squad cache for a specific match or all matches.
     */
    fun clearCache(matchId: String? = null) {
        if (matchId != null) squadCache.remove(matchId)
        else squadCache.clear()
    }
}