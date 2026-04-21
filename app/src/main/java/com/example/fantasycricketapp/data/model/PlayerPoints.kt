package com.example.fantasycricketapp.data.model


/**
 * PlayerPoints — Represents mock fantasy points awarded to a player
 * for their performance in a specific match.
 *
 * Firestore path: points/{matchId}_{playerId}
 *
 * Points breakdown (standard fantasy cricket scoring):
 * ─────────────────────────────────────────────────────
 * Batting:
 *   Run scored       → +1 pt each
 *   Boundary (4)     → +1 bonus
 *   Six (6)          → +2 bonus
 *   50 runs          → +8 bonus
 *   100 runs         → +16 bonus
 *   Duck (0)         → -2 pts
 *
 * Bowling:
 *   Wicket           → +25 pts each
 *   Maiden over      → +8 pts each
 *   4 wickets        → +8 bonus
 *   5 wickets        → +16 bonus
 *
 * Fielding:
 *   Catch            → +8 pts each
 *   Stumping         → +12 pts
 *   Run-out          → +6 pts
 *
 * totalPoints = sum of all above
 */
data class PlayerPoints(
    val pointsId: String = "",          // "{matchId}_{playerId}"
    val matchId: String = "",
    val playerId: String = "",
    val playerName: String = "",
    val team: String = "",
    val role: String = "",

    // Batting stats
    val runsScored: Int = 0,
    val ballsFaced: Int = 0,
    val fours: Int = 0,
    val sixes: Int = 0,
    val isDuck: Boolean = false,

    // Bowling stats
    val wicketsTaken: Int = 0,
    val oversBowled: Double = 0.0,
    val maidenOvers: Int = 0,
    val economyRate: Double = 0.0,

    // Fielding stats
    val catches: Int = 0,
    val stumpings: Int = 0,
    val runOuts: Int = 0,

    // Final computed total
    val totalPoints: Int = 0
)