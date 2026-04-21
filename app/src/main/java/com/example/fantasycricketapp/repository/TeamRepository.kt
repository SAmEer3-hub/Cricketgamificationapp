package com.example.fantasycricketapp.data.repository

import com.example.fantasycricketapp.data.model.Player
import com.example.fantasycricketapp.data.model.UserTeam
import com.example.fantasycricketapp.utils.FirebaseHelper

class TeamRepository {

    private val firestore = FirebaseHelper.firestore
    private val teamsCollection = FirebaseHelper.COLLECTION_TEAMS

    // SAVE TEAM
    fun saveTeam(
        matchId: String,
        matchName: String,
        selectedPlayers: List<Player>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val userId = FirebaseHelper.getCurrentUserId()

        if (userId.isEmpty()) {
            onFailure("User not logged in. Please login again.")
            return
        }

        if (selectedPlayers.size != 11) {
            onFailure("Please select exactly 11 players.")
            return
        }

        val teamId = "${userId}_${matchId}"
        val totalCredits = selectedPlayers.sumOf { it.creditPoints }
        val playerIds = selectedPlayers.map { it.playerId }

        val userTeam = UserTeam(
            teamId = teamId,
            userId = userId,
            matchId = matchId,
            matchName = matchName,
            selectedPlayerIds = playerIds,
            totalCredits = totalCredits,
            createdAt = System.currentTimeMillis()
        )

        firestore.collection(teamsCollection)
            .document(teamId)
            .set(userTeam)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                onFailure(it.message ?: "Failed to save team. Try again.")
            }
    }

    // CHECK TEAM EXISTS
    fun checkTeamExists(
        matchId: String,
        onResult: (Boolean) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val userId = FirebaseHelper.getCurrentUserId()
        val teamId = "${userId}_${matchId}"

        firestore.collection(teamsCollection)
            .document(teamId)
            .get()
            .addOnSuccessListener { onResult(it.exists()) }
            .addOnFailureListener {
                onFailure(it.message ?: "Failed to check team status.")
            }
    }

    // FETCH TEAM
    fun fetchTeam(
        matchId: String,
        onSuccess: (UserTeam) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val userId = FirebaseHelper.getCurrentUserId()
        val teamId = "${userId}_${matchId}"

        firestore.collection(teamsCollection)
            .document(teamId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val team = document.toObject(UserTeam::class.java)
                    if (team != null) {
                        onSuccess(team)
                    } else {
                        onFailure("Failed to parse team data.")
                    }
                } else {
                    onFailure("No team found for this match.")
                }
            }
            .addOnFailureListener {
                onFailure(it.message ?: "Failed to load team.")
            }
    }
}