package com.example.fantasycricketapp.data.model


/**
 * LeagueMessage — A single chat message inside a league.
 *
 * Firestore path: leagues/{leagueCode}/messages/{messageId}
 *
 * Stored in a subcollection so messages don't inflate
 * the parent League document size.
 */
data class LeagueMessage(
    val messageId: String = "",
    val leagueCode: String = "",
    val userId: String = "",
    val userName: String = "",          // Display name for the bubble header
    val text: String = "",
    val timestamp: Long = 0L
)