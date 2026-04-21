package com.example.fantasycricketapp.data.model

/**
 * Contest — Represents a 1 vs 1 fantasy cricket contest between two users.
 *
 * Firestore path: contests/{contestId}
 *
 * Contest lifecycle:
 *   "WAITING"    → User1 created contest, waiting for User2 to join
 *   "ACTIVE"     → Both users joined, contest is live
 *   "COMPLETED"  → Points calculated, winner declared
 *
 * contestId format: "{matchId}_{user1Id}" — ensures one contest per user per match
 */
data class Contest(
    val contestId: String = "",         // Unique contest ID
    val matchId: String = "",           // Which match this contest is for
    val matchName: String = "",         // Human-readable match name

    // User 1 — the one who CREATED the contest
    val user1Id: String = "",
    val user1Name: String = "",         // Display name/email prefix
    val user1TeamId: String = "",       // "{user1Id}_{matchId}"
    val user1Score: Int = 0,            // Points scored (set in Phase 6)

    // User 2 — the one who JOINS the contest
    val user2Id: String = "",
    val user2Name: String = "",
    val user2TeamId: String = "",       // "{user2Id}_{matchId}"
    val user2Score: Int = 0,            // Points scored (set in Phase 6)

    val status: String = "WAITING",     // "WAITING", "ACTIVE", "COMPLETED"
    val winnerId: String = "",          // UID of winner (set in Phase 6)
    val createdAt: Long = 0L            // Epoch timestamp
)