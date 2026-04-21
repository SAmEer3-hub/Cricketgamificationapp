package com.example.fantasycricketapp.data.model


/**
 * Player — Data model representing a cricket player.
 *
 * Role values:
 *   "BAT"  → Batsman
 *   "BOWL" → Bowler
 *   "AR"   → All-Rounder
 *   "WK"   → Wicket-Keeper
 *
 * creditPoints → cost to pick this player (for future salary cap feature)
 * isSelected   → NOT stored in Firestore (UI-only state, managed in adapter)
 */
data class Player(
    val playerId: String = "",
    val name: String = "",
    val team: String = "",          // Short team code e.g. "IND", "AUS"
    val role: String = "",          // "BAT", "BOWL", "AR", "WK"
    val creditPoints: Double = 8.0, // Player cost (6.0 to 10.0)
    val battingStyle: String = "",  // e.g. "Right-hand bat"
    val bowlingStyle: String = "",  // e.g. "Right-arm fast" or "N/A"
    val isSelected: Boolean = false // UI-only — not saved to Firestore
)