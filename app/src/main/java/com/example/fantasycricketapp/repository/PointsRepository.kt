package com.example.fantasycricketapp.repository

import com.example.fantasycricketapp.data.model.Contest
import com.example.fantasycricketapp.data.model.PlayerPoints
import com.example.fantasycricketapp.utils.FirebaseHelper
import com.example.fantasycricketapp.utils.MockPointsData

class PointsRepository {

    private val firestore   = FirebaseHelper.firestore
    private val teamsCol    = FirebaseHelper.COLLECTION_TEAMS
    private val contestsCol = FirebaseHelper.COLLECTION_CONTESTS


    // CALCULATE AND SAVE RESULT


    fun calculateAndSaveResult(
        contest: Contest,
        onSuccess: (Contest) -> Unit,
        onFailure: (String) -> Unit
    ) {

        if (contest.user1Id.isEmpty() || contest.user2Id.isEmpty()) {
            onFailure("Contest is not ACTIVE yet — opponent hasn't joined.")
            return
        }

        val matchId = contest.matchId

        fetchPlayerIdsForTeam(
            teamId = contest.user1TeamId,
            onSuccess = { user1PlayerIds ->

                fetchPlayerIdsForTeam(
                    teamId = contest.user2TeamId,
                    onSuccess = { user2PlayerIds ->

                        val allPoints: List<PlayerPoints> =
                            MockPointsData.getPointsForMatch(matchId)

                        val user1Score = calculateTeamScore(user1PlayerIds, allPoints)
                        val user2Score = calculateTeamScore(user2PlayerIds, allPoints)

                        val winnerId = when {
                            user1Score > user2Score -> contest.user1Id
                            user2Score > user1Score -> contest.user2Id
                            else -> "TIE"
                        }

                        saveResultToFirestore(
                            contestId = contest.contestId,
                            user1Score = user1Score,
                            user2Score = user2Score,
                            winnerId = winnerId,
                            onSuccess = {
                                val updatedContest = contest.copy(
                                    user1Score = user1Score,
                                    user2Score = user2Score,
                                    winnerId = winnerId,
                                    status = "COMPLETED"
                                )
                                onSuccess(updatedContest)
                            },
                            onFailure = onFailure
                        )
                    },
                    onFailure = {
                        onFailure("Could not load opponent's team: $it")
                    }
                )
            },
            onFailure = {
                onFailure("Could not load your team: $it")
            }
        )
    }


    // FETCH PLAYER IDS


    private fun fetchPlayerIdsForTeam(
        teamId: String,
        onSuccess: (List<String>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore
            .collection(teamsCol)
            .document(teamId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    @Suppress("UNCHECKED_CAST")
                    val playerIds = document.get("selectedPlayerIds") as? List<String>
                        ?: emptyList()
                    onSuccess(playerIds)
                } else {
                    onFailure("Team not found")
                }
            }
            .addOnFailureListener {
                onFailure(it.message ?: "Failed to fetch team")
            }
    }


    // CALCULATE SCORE


    fun calculateTeamScore(
        playerIds: List<String>,
        allPoints: List<PlayerPoints>
    ): Int {

        val pointsMap: Map<String, PlayerPoints> =
            allPoints.associateBy { it.playerId }

        return playerIds.sumOf { playerId ->
            pointsMap[playerId]?.totalPoints ?: 0
        }
    }

    // ----------------------------------------------------------
    // GET PLAYER BREAKDOWN
    // ----------------------------------------------------------

    fun getTeamPlayerPoints(
        playerIds: List<String>,
        matchId: String
    ): List<PlayerPoints> {

        val allPoints: List<PlayerPoints> =
            MockPointsData.getPointsForMatch(matchId)

        val pointsMap: Map<String, PlayerPoints> =
            allPoints.associateBy { it.playerId }

        return playerIds
            .mapNotNull { playerId -> pointsMap[playerId] }
            .sortedByDescending { it.totalPoints }
    }

    // ----------------------------------------------------------
    // SAVE RESULT
    // ----------------------------------------------------------

    private fun saveResultToFirestore(
        contestId: String,
        user1Score: Int,
        user2Score: Int,
        winnerId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {

        val updates = mapOf(
            "user1Score" to user1Score,
            "user2Score" to user2Score,
            "winnerId"   to winnerId,
            "status"     to "COMPLETED"
        )

        firestore
            .collection(contestsCol)
            .document(contestId)
            .update(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                onFailure(it.message ?: "Failed to save result")
            }
    }

    // ----------------------------------------------------------
    // PUBLIC ACCESS
    // ----------------------------------------------------------

    fun getPlayerIdsForTeam(
        teamId: String,
        onSuccess: (List<String>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        fetchPlayerIdsForTeam(teamId, onSuccess, onFailure)
    }
}