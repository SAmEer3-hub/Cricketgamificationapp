package com.example.fantasycricketapp.data.model


/**
 * League — Represents a private 1 vs 1 fantasy cricket league.
 *
 * Firestore path: leagues/{leagueCode}
 *
 * The document ID is the invite code itself (e.g. "FCK284").
 * This makes "join by code" a simple direct document lookup
 * instead of a query — much faster and cheaper.
 *
 * Status lifecycle:
 *   "OPEN"      → Created, waiting for opponent to join
 *   "ACTIVE"    → Both players joined, contest in progress
 *   "COMPLETED" → Scores calculated, winner declared
 */
data class League(
    val leagueCode: String = "",        // Unique invite code — also the document ID
    val leagueName: String = "",        // Name chosen by creator e.g. "Virat vs Babar"
    val matchId: String = "",
    val matchName: String = "",
    val creatorId: String = "",
    val creatorName: String = "",
    val members: List<LeagueMember> = emptyList(),  // Max 2 members for 1v1
    val status: String = "OPEN",        // "OPEN", "ACTIVE", "COMPLETED"
    val winnerId: String = "",          // UID of winner, or "TIE"
    val maxMembers: Int = 2,
    val createdAt: Long = 0L
)