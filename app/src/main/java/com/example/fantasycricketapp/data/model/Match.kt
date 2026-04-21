package com.example.fantasycricketapp.data.model

/**
 * Match — Data model representing a cricket match.
 *
 * Default values are required so Firebase can deserialize
 * documents automatically using reflection.
 *
 * Status values:
 *   "UPCOMING"  → match hasn't started yet
 *   "LIVE"      → match is currently in progress
 *   "COMPLETED" → match has ended
 */
data class Match(
    val matchId: String = "",           // Unique match identifier
    val matchName: String = "",         // e.g. "India vs Australia"
    val team1: String = "",             // Full name of team 1
    val team2: String = "",             // Full name of team 2
    val team1Short: String = "",        // Short code e.g. "IND"
    val team2Short: String = "",        // Short code e.g. "AUS"
    val team1Flag: String = "",         // Emoji flag e.g. "🇮🇳"
    val team2Flag: String = "",         // Emoji flag e.g. "🇦🇺"
    val venue: String = "",             // Stadium name
    val matchDate: String = "",         // Display date string e.g. "25 Jan, 2:30 PM"
    val matchType: String = "",         // "T20", "ODI", "Test"
    val status: String = "UPCOMING"     // "UPCOMING", "LIVE", "COMPLETED"
)