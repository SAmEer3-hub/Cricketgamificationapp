package com.example.fantasycricketapp.data.model


/**
 * UserTeam — Represents a user's selected team for a specific match.
 *
 * Firestore path: teams/{userId}_{matchId}
 *
 * This document stores:
 * - Which user created the team
 * - Which match it's for
 * - List of selected player IDs
 * - Total credits used
 * - Timestamp of creation
 */
data class UserTeam(
    val teamId: String = "",            // "{userId}_{matchId}"
    val userId: String = "",            // Firebase Auth UID
    val matchId: String = "",           // Match this team is for
    val matchName: String = "",         // Human-readable match name
    val selectedPlayerIds: List<String> = emptyList(), // 11 player IDs
    val totalCredits: Double = 0.0,     // Sum of all selected player credits
    val createdAt: Long = 0L            // Epoch timestamp
)