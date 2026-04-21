package com.example.fantasycricketapp.data.model.api


import com.google.gson.annotations.SerializedName

// ══════════════════════════════════════════════════════════════
// ROOT RESPONSE WRAPPERS
// CricAPI wraps all responses in the same outer structure
// ══════════════════════════════════════════════════════════════

/**
 * Generic wrapper for all CricAPI list responses.
 * T is the type of items in the "data" array.
 *
 * Example JSON:
 * {
 *   "apikey": "...",
 *   "status": "success",
 *   "data": [...],
 *   "info": { "hitsToday": 5, "hitsUsed": 5, "credits": 0 }
 * }
 */
data class CricApiListResponse<T>(
    @SerializedName("apikey")  val apiKey: String? = null,
    @SerializedName("status")  val status: String? = null,
    @SerializedName("data")    val data: List<T>? = null,
    @SerializedName("info")    val info: ApiInfo? = null,
    @SerializedName("message") val message: String? = null   // Set when status = "failure"
)

/**
 * Generic wrapper for single-object responses (match_info, match_squad).
 */
data class CricApiObjectResponse<T>(
    @SerializedName("apikey")  val apiKey: String? = null,
    @SerializedName("status")  val status: String? = null,
    @SerializedName("data")    val data: T? = null,
    @SerializedName("info")    val info: ApiInfo? = null,
    @SerializedName("message") val message: String? = null
)

/**
 * API usage info included in every response.
 * Useful for tracking how many free calls you have left.
 */
data class ApiInfo(
    @SerializedName("hitsToday")     val hitsToday: Int = 0,
    @SerializedName("hitsUsed")      val hitsUsed: Int = 0,
    @SerializedName("totalRows")     val totalRows: Int = 0,
    @SerializedName("queryTime")     val queryTime: Double = 0.0,
    @SerializedName("s")             val offset: Int = 0,
    @SerializedName("a")             val limit: Int = 0
)

// ══════════════════════════════════════════════════════════════
// MATCH DATA MODELS
// ══════════════════════════════════════════════════════════════

/**
 * ApiMatch — Represents one match from /currentMatches or /matches endpoint.
 *
 * Key fields we use:
 *   id          → unique match identifier (used for squad lookup)
 *   name        → "India vs Australia, 1st T20I"
 *   status      → "Match not started" / "India won by 5 wickets" etc.
 *   venue       → Stadium name
 *   date        → ISO date string "2025-01-25"
 *   dateTimeGMT → Full datetime string
 *   teams       → ["India", "Australia"]
 *   matchType   → "t20" / "odi" / "test"
 *   matchStarted→ boolean
 *   matchEnded  → boolean
 *   score       → live score array (when match is running)
 */
data class ApiMatch(
    @SerializedName("id")           val id: String = "",
    @SerializedName("name")         val name: String = "",
    @SerializedName("status")       val status: String = "",
    @SerializedName("venue")        val venue: String = "",
    @SerializedName("date")         val date: String = "",
    @SerializedName("dateTimeGMT")  val dateTimeGMT: String = "",
    @SerializedName("teams")        val teams: List<String> = emptyList(),
    @SerializedName("teamInfo")     val teamInfo: List<ApiTeamInfo>? = null,
    @SerializedName("score")        val score: List<ApiScore>? = null,
    @SerializedName("series_id")    val seriesId: String = "",
    @SerializedName("matchType")    val matchType: String = "",
    @SerializedName("matchStarted") val matchStarted: Boolean = false,
    @SerializedName("matchEnded")   val matchEnded: Boolean = false
)

/**
 * ApiTeamInfo — Short info for each team in a match.
 * Contains team short name and flag image URL.
 */
data class ApiTeamInfo(
    @SerializedName("name")      val name: String = "",
    @SerializedName("shortname") val shortName: String = "",
    @SerializedName("img")       val imgUrl: String = ""
)

/**
 * ApiScore — Live/final score for one innings.
 */
data class ApiScore(
    @SerializedName("r")        val runs: Int = 0,
    @SerializedName("w")        val wickets: Int = 0,
    @SerializedName("o")        val overs: Double = 0.0,
    @SerializedName("inning")   val inning: String = ""
)

// ══════════════════════════════════════════════════════════════
// SQUAD / PLAYER DATA MODELS
// ══════════════════════════════════════════════════════════════

/**
 * ApiMatchSquad — Response from /match_squad endpoint.
 * Contains two teams each with a list of players.
 */
data class ApiMatchSquad(
    @SerializedName("id")       val matchId: String = "",
    @SerializedName("name")     val matchName: String = "",
    @SerializedName("teams")    val teams: List<String> = emptyList(),
    @SerializedName("teamInfo") val teamInfo: List<ApiTeamInfo>? = null,
    @SerializedName("squad")    val squads: List<ApiSquad>? = null
)

/**
 * ApiSquad — One team's squad inside a match.
 */
data class ApiSquad(
    @SerializedName("name")   val teamName: String = "",
    @SerializedName("players")val players: List<ApiPlayer>? = null
)

/**
 * ApiPlayer — One player from the squad.
 *
 * Fields we use:
 *   id   → unique player id
 *   name → full player name
 *   role → "Batsman" / "Bowler" / "All-Rounder" / "WK-Batsman"
 */
data class ApiPlayer(
    @SerializedName("id")           val id: String = "",
    @SerializedName("name")         val name: String = "",
    @SerializedName("role")         val role: String = "",
    @SerializedName("battingStyle") val battingStyle: String = "",
    @SerializedName("bowlingStyle") val bowlingStyle: String = "",
    @SerializedName("country")      val country: String = "",
    @SerializedName("playerImg")    val playerImg: String = ""
)

// ══════════════════════════════════════════════════════════════
// SCORECARD DATA MODELS (for points calculation)
// ══════════════════════════════════════════════════════════════

/**
 * ApiMatchInfo — Detailed match info from /match_info endpoint.
 * Used to get scorecard data for points calculation.
 */
data class ApiMatchInfo(
    @SerializedName("id")         val id: String = "",
    @SerializedName("name")       val name: String = "",
    @SerializedName("status")     val status: String = "",
    @SerializedName("venue")      val venue: String = "",
    @SerializedName("date")       val date: String = "",
    @SerializedName("teams")      val teams: List<String> = emptyList(),
    @SerializedName("teamInfo")   val teamInfo: List<ApiTeamInfo>? = null,
    @SerializedName("score")      val score: List<ApiScore>? = null,
    @SerializedName("tossWinner") val tossWinner: String = "",
    @SerializedName("tossChoice") val tossChoice: String = "",
    @SerializedName("matchWinner")val matchWinner: String = "",
    @SerializedName("matchType")  val matchType: String = "",
    @SerializedName("matchStarted")val matchStarted: Boolean = false,
    @SerializedName("matchEnded") val matchEnded: Boolean = false,
    @SerializedName("scorecard")  val scorecard: List<ApiInnings>? = null
)

/**
 * ApiInnings — One innings scorecard (batting + bowling).
 */
data class ApiInnings(
    @SerializedName("inning")   val inningName: String = "",
    @SerializedName("batsman")  val batsmen: List<ApiBatsmanScore>? = null,
    @SerializedName("bowler")   val bowlers: List<ApiBowlerScore>? = null,
    @SerializedName("extras")   val extras: ApiExtras? = null
)

/**
 * ApiBatsmanScore — Individual batsman's scorecard entry.
 */
data class ApiBatsmanScore(
    @SerializedName("batsman")    val playerName: String = "",
    @SerializedName("r")          val runs: String = "0",
    @SerializedName("b")          val balls: String = "0",
    @SerializedName("4s")         val fours: String = "0",
    @SerializedName("6s")         val sixes: String = "0",
    @SerializedName("sr")         val strikeRate: String = "0"
)

/**
 * ApiBowlerScore — Individual bowler's scorecard entry.
 */
data class ApiBowlerScore(
    @SerializedName("bowler")     val playerName: String = "",
    @SerializedName("o")          val overs: String = "0",
    @SerializedName("m")          val maidens: String = "0",
    @SerializedName("r")          val runs: String = "0",
    @SerializedName("w")          val wickets: String = "0",
    @SerializedName("eco")        val economy: String = "0"
)

/**
 * ApiExtras — Extras in an innings.
 */
data class ApiExtras(
    @SerializedName("r") val runs: Int = 0
)