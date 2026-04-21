package com.example.fantasycricketapp.data.model

/**
 * LeagueMember — Represents one participant inside a private league.
 *
 * Stored as a List inside the League document (not a subcollection)
 * because with only 2 members this is simpler and more efficient.
 *
 * hasTeam → true once the member submits their 11 players via TeamSelectionActivity.
 * score   → set to 0 until league scores are calculated (status = COMPLETED).
 */
data class LeagueMember(
    val userId: String = "",
    val userName: String = "",          // Email prefix e.g. "john"
    val teamId: String = "",            // "{userId}_{matchId}" — links to teams collection
    val score: Int = 0,                 // Fantasy score after calculation
    val hasTeam: Boolean = false,       // Has this member submitted their team?
    val joinedAt: Long = 0L             // Epoch timestamp of when they joined
)