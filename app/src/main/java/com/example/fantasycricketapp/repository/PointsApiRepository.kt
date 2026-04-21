package com.example.fantasycricketapp.repository

import android.util.Log
import com.example.fantasycricketapp.data.model.PlayerPoints
import com.example.fantasycricketapp.data.model.api.ApiBatsmanScore
import com.example.fantasycricketapp.data.model.api.ApiBowlerScore
import com.example.fantasycricketapp.data.model.api.ApiInnings
import com.example.fantasycricketapp.data.data.remote.CricApiClient
import com.example.fantasycricketapp.utils.ApiConstants
import com.example.fantasycricketapp.utils.ApiResult
import com.example.fantasycricketapp.utils.MockPointsData

/**
 * PointsApiRepository — Fetches real scorecard data and converts to fantasy points.
 *
 * This is the most complex repository because it:
 *   1. Fetches the full scorecard from /match_info
 *   2. Parses batting stats per player (runs, fours, sixes)
 *   3. Parses bowling stats per player (wickets, maidens)
 *   4. Applies the standard fantasy scoring formula
 *   5. Returns a PlayerPoints list identical in structure to MockPointsData
 *
 * Fallback:
 *   If the match hasn't ended yet or the API call fails,
 *   falls back to MockPointsData for that matchId.
 *
 * Fantasy Scoring Formula (same as Phase 6 calcPoints()):
 *   Batting:  +1 per run, +1 per four, +2 per six, +8 for 50, +16 for 100, -2 for duck
 *   Bowling:  +25 per wicket, +8 per maiden, +8 for 4wkt haul, +16 for 5wkt haul
 *   Fielding: API doesn't provide catch/stumping data reliably
 *             → we add 0 fielding points from API (handled by MockPointsData fallback)
 */
class PointsApiRepository {

    private val tag = "PointsApiRepository"
    private val apiService = CricApiClient.service

    // Cache: matchId → PlayerPoints list
    private val pointsCache = mutableMapOf<String, List<PlayerPoints>>()

    // ----------------------------------------------------------
    // FETCH POINTS — Main entry point
    // ----------------------------------------------------------

    /**
     * Returns fantasy points for all players in a match.
     *
     * @param matchId  The CricAPI match ID
     */
    suspend fun getPointsForMatch(matchId: String): ApiResult<List<PlayerPoints>> {

        // ── Cache check ──
        pointsCache[matchId]?.let {
            Log.d(tag, "Returning cached points for $matchId")
            return ApiResult.Success(it)
        }

        return try {
            Log.d(tag, "Fetching scorecard for match: $matchId")

            val response = apiService.getMatchInfo(
                apiKey  = ApiConstants.API_KEY,
                matchId = matchId
            )

            when {
                response.isSuccessful -> {
                    val body = response.body()

                    when {
                        body?.status == "success" && body.data != null -> {
                            val matchInfo = body.data

                            // ── Match must be ended to get full scorecard ──
                            if (!matchInfo.matchEnded) {
                                Log.d(tag, "Match not ended yet — using live scoring approach")
                                // Still try to parse partial scorecard for live matches
                            }

                            val scorecard = matchInfo.scorecard
                            if (scorecard.isNullOrEmpty()) {
                                Log.w(tag, "No scorecard data — using MockPointsData")
                                return ApiResult.Success(
                                    MockPointsData.getPointsForMatch(matchId)
                                )
                            }

                            // ── Parse scorecard into PlayerPoints ──
                            val playerPoints = parseScorecardToPoints(matchId, scorecard)

                            if (playerPoints.isEmpty()) {
                                Log.w(tag, "Parsed 0 players — using MockPointsData")
                                return ApiResult.Success(
                                    MockPointsData.getPointsForMatch(matchId)
                                )
                            }

                            Log.d(tag, "Parsed points for ${playerPoints.size} players")

                            // Cache it
                            pointsCache[matchId] = playerPoints

                            ApiResult.Success(playerPoints)
                        }

                        body?.status == "failure" -> {
                            Log.w(tag, "Points API failure: ${body.message} — using fallback")
                            ApiResult.Success(MockPointsData.getPointsForMatch(matchId))
                        }

                        else -> {
                            Log.w(tag, "Empty scorecard — using MockPointsData")
                            ApiResult.Success(MockPointsData.getPointsForMatch(matchId))
                        }
                    }
                }

                else -> {
                    Log.e(tag, "HTTP ${response.code()} — using MockPointsData")
                    ApiResult.Success(MockPointsData.getPointsForMatch(matchId))
                }
            }

        } catch (e: java.net.UnknownHostException) {
            Log.w(tag, "No internet — using MockPointsData")
            ApiResult.Success(MockPointsData.getPointsForMatch(matchId))
        } catch (e: Exception) {
            Log.e(tag, "Error fetching points: ${e.message} — using MockPointsData")
            ApiResult.Success(MockPointsData.getPointsForMatch(matchId))
        }
    }

    // ----------------------------------------------------------
    // PARSE SCORECARD → PlayerPoints
    // ----------------------------------------------------------

    /**
     * Parses a list of innings scorecards into individual PlayerPoints objects.
     *
     * Handles:
     *   - Multiple innings (T20: 2, ODI: 2, Test: up to 4)
     *   - Players who bat in one innings and bowl in another
     *   - Merges stats across innings for the same player
     */
    private fun parseScorecardToPoints(
        matchId: String,
        scorecard: List<ApiInnings>
    ): List<PlayerPoints> {

        // Map: playerName → accumulated stats
        val playerStatsMap = mutableMapOf<String, PlayerStatAccumulator>()

        scorecard.forEach { innings ->
            // ── Parse batting stats ──
            innings.batsmen?.forEach { batsmanScore ->
                val name = batsmanScore.playerName.trim()
                if (name.isBlank()) return@forEach

                val acc = playerStatsMap.getOrPut(name) { PlayerStatAccumulator(name) }
                acc.runsScored  += batsmanScore.runs.toIntSafe()
                acc.ballsFaced  += batsmanScore.balls.toIntSafe()
                acc.fours       += batsmanScore.fours.toIntSafe()
                acc.sixes       += batsmanScore.sixes.toIntSafe()

                // Duck detection — batted, got 0, and is out
                if (acc.runsScored == 0 && batsmanScore.balls.toIntSafe() > 0) {
                    acc.isDuck = true
                }
            }

            // ── Parse bowling stats ──
            innings.bowlers?.forEach { bowlerScore ->
                val name = bowlerScore.playerName.trim()
                if (name.isBlank()) return@forEach

                val acc = playerStatsMap.getOrPut(name) { PlayerStatAccumulator(name) }
                acc.wicketsTaken += bowlerScore.wickets.toIntSafe()
                acc.maidenOvers  += bowlerScore.maidens.toIntSafe()

                // Parse overs (e.g. "3.4" = 3 complete overs + 4 balls)
                val oversDouble = bowlerScore.overs.toDoubleSafe()
                acc.oversBowled += oversDouble
            }
        }

        // ── Convert accumulators to PlayerPoints ──
        return playerStatsMap.values.map { acc ->
            val totalPoints = calcFantasyPoints(
                runs     = acc.runsScored,
                fours    = acc.fours,
                sixes    = acc.sixes,
                isDuck   = acc.isDuck,
                wickets  = acc.wicketsTaken,
                maidens  = acc.maidenOvers
            )

            PlayerPoints(
                pointsId     = "${matchId}_${acc.playerName.replace(" ", "_")}",
                matchId      = matchId,
                playerId     = acc.playerName.replace(" ", "_").lowercase(),
                playerName   = acc.playerName,
                team         = "",       // Team info not available in scorecard directly
                role         = inferRole(acc),
                runsScored   = acc.runsScored,
                ballsFaced   = acc.ballsFaced,
                fours        = acc.fours,
                sixes        = acc.sixes,
                isDuck       = acc.isDuck,
                wicketsTaken = acc.wicketsTaken,
                maidenOvers  = acc.maidenOvers,
                oversBowled  = acc.oversBowled,
                catches      = 0,        // Not reliably available from CricAPI free tier
                stumpings    = 0,
                runOuts      = 0,
                totalPoints  = totalPoints
            )
        }.sortedByDescending { it.totalPoints }
    }

    // ----------------------------------------------------------
    // FANTASY POINTS FORMULA
    // ----------------------------------------------------------

    /**
     * Identical scoring formula to Phase 6's calcPoints().
     * Kept here so both sources (API + Mock) use the same rules.
     */
    private fun calcFantasyPoints(
        runs: Int = 0, fours: Int = 0, sixes: Int = 0, isDuck: Boolean = false,
        wickets: Int = 0, maidens: Int = 0,
        catches: Int = 0, stumpings: Int = 0, runOuts: Int = 0
    ): Int {
        var pts = 0

        // Batting
        pts += runs
        pts += fours
        pts += sixes * 2
        if (runs >= 100) pts += 16
        else if (runs >= 50) pts += 8
        if (isDuck) pts -= 2

        // Bowling
        pts += wickets * 25
        pts += maidens * 8
        if (wickets >= 5) pts += 16
        else if (wickets >= 4) pts += 8

        // Fielding
        pts += catches * 8
        pts += stumpings * 12
        pts += runOuts * 6

        return maxOf(pts, 0) // Never negative total
    }

    /**
     * Infers a player's role from their accumulated stats.
     * Not perfect but good enough when API doesn't provide role in scorecard.
     */
    private fun inferRole(acc: PlayerStatAccumulator): String {
        val hasBatted  = acc.runsScored > 0 || acc.ballsFaced > 0
        val hasBowled  = acc.wicketsTaken > 0 || acc.oversBowled > 0
        return when {
            hasBatted && hasBowled -> "AR"
            hasBowled              -> "BOWL"
            else                   -> "BAT"
        }
    }

    // ----------------------------------------------------------
    // HELPER: Stat accumulator per player
    // ----------------------------------------------------------

    private data class PlayerStatAccumulator(
        val playerName: String,
        var runsScored: Int = 0,
        var ballsFaced: Int = 0,
        var fours: Int = 0,
        var sixes: Int = 0,
        var isDuck: Boolean = false,
        var wicketsTaken: Int = 0,
        var maidenOvers: Int = 0,
        var oversBowled: Double = 0.0
    )

    // ----------------------------------------------------------
    // EXTENSION HELPERS — Safe string → number parsing
    // ----------------------------------------------------------

    private fun String.toIntSafe(): Int = this.trim().toIntOrNull() ?: 0
    private fun String.toDoubleSafe(): Double = this.trim().toDoubleOrNull() ?: 0.0

    fun clearCache(matchId: String? = null) {
        if (matchId != null) pointsCache.remove(matchId)
        else pointsCache.clear()
    }
}