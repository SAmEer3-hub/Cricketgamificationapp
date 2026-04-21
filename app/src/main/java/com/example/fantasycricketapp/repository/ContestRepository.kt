package com.example.fantasycricketapp.repository

import com.google.firebase.firestore.Query
import com.example.fantasycricketapp.data.model.Contest
import com.example.fantasycricketapp.utils.FirebaseHelper

class ContestRepository {

    private val firestore = FirebaseHelper.firestore
    private val contestsCollection = FirebaseHelper.COLLECTION_CONTESTS

    // ----------------------------------------------------------
    // CREATE CONTEST
    // ----------------------------------------------------------

    fun createContest(
        matchId: String,
        matchName: String,
        onSuccess: (Contest) -> Unit,
        onFailure: (String) -> Unit
    ) {
        var userId = FirebaseHelper.getCurrentUserId()
        val userEmail = FirebaseHelper.getCurrentUser()?.email ?: "Player"

        // FIX: Allow guest user
        if (userId.isEmpty()) {
            userId = "guest_user"
        }

        val userName = userEmail.substringBefore("@")

        val contestId = "${matchId}_${userId}"
        val teamId = "${userId}_${matchId}"

        val contest = Contest(
            contestId = contestId,
            matchId = matchId,
            matchName = matchName,
            user1Id = userId,
            user1Name = userName,
            user1TeamId = teamId,
            user1Score = 0,
            user2Id = "",
            user2Name = "",
            user2TeamId = "",
            user2Score = 0,
            status = "WAITING",
            winnerId = "",
            createdAt = System.currentTimeMillis()
        )

        firestore
            .collection(contestsCollection)
            .document(contestId)
            .set(contest)
            .addOnSuccessListener {
                onSuccess(contest)
            }
            .addOnFailureListener { exception ->
                onFailure(exception.message ?: "Failed to create contest.")
            }
    }

    // ----------------------------------------------------------
    // JOIN OR CREATE CONTEST
    // ----------------------------------------------------------

    fun joinOrCreateContest(
        matchId: String,
        matchName: String,
        onJoined: (Contest) -> Unit,
        onCreated: (Contest) -> Unit,
        onAlreadyInContest: (Contest) -> Unit,
        onFailure: (String) -> Unit
    ) {
        var userId = FirebaseHelper.getCurrentUserId()

        // FIX
        if (userId.isEmpty()) {
            userId = "guest_user"
        }

        checkUserAlreadyInContest(
            matchId = matchId,
            userId = userId,
            onResult = { existingContest ->
                if (existingContest != null) {
                    onAlreadyInContest(existingContest)
                } else {
                    findAvailableContest(
                        matchId = matchId,
                        userId = userId,
                        onFound = { availableContest ->
                            joinExistingContest(
                                contest = availableContest,
                                onSuccess = onJoined,
                                onFailure = onFailure
                            )
                        },
                        onNotFound = {
                            createContest(
                                matchId = matchId,
                                matchName = matchName,
                                onSuccess = onCreated,
                                onFailure = onFailure
                            )
                        },
                        onFailure = onFailure
                    )
                }
            },
            onFailure = onFailure
        )
    }


    // CHECK USER ALREADY IN CONTEST


    private fun checkUserAlreadyInContest(
        matchId: String,
        userId: String,
        onResult: (Contest?) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val user1ContestId = "${matchId}_${userId}"

        firestore
            .collection(contestsCollection)
            .document(user1ContestId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val contest = doc.toObject(Contest::class.java)
                    onResult(contest)
                } else {
                    checkIfUserIsPlayer2(
                        matchId,
                        userId,
                        onResult,
                        onFailure
                    )
                }
            }
            .addOnFailureListener {
                onFailure("Failed to check contest.")
            }
    }

    private fun checkIfUserIsPlayer2(
        matchId: String,
        userId: String,
        onResult: (Contest?) -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore
            .collection(contestsCollection)
            .whereEqualTo("matchId", matchId)
            .whereEqualTo("user2Id", userId)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                val contest = snapshot.documents
                    .firstOrNull()
                    ?.toObject(Contest::class.java)
                onResult(contest)
            }
            .addOnFailureListener {
                onFailure("Failed to check Player 2 status.")
            }
    }


    // FIND AVAILABLE CONTEST


    private fun findAvailableContest(
        matchId: String,
        userId: String,
        onFound: (Contest) -> Unit,
        onNotFound: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore
            .collection(contestsCollection)
            .whereEqualTo("matchId", matchId)
            .whereEqualTo("status", "WAITING")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener { snapshot ->

                val available = snapshot.documents
                    .mapNotNull { it.toObject(Contest::class.java) }
                    .filter { it.user1Id != userId }
                    .firstOrNull()

                if (available != null) {
                    onFound(available)
                } else {
                    onNotFound()
                }
            }
            .addOnFailureListener {
                onFailure("Failed to search for contests.")
            }
    }

    // ----------------------------------------------------------
    // JOIN EXISTING CONTEST
    // ----------------------------------------------------------

    private fun joinExistingContest(
        contest: Contest,
        onSuccess: (Contest) -> Unit,
        onFailure: (String) -> Unit
    ) {
        var userId = FirebaseHelper.getCurrentUserId()
        val userEmail = FirebaseHelper.getCurrentUser()?.email ?: "Player"

        // FIX
        if (userId.isEmpty()) {
            userId = "guest_user"
        }

        val userName = userEmail.substringBefore("@")
        val teamId = "${userId}_${contest.matchId}"

        val updates = mapOf(
            "user2Id" to userId,
            "user2Name" to userName,
            "user2TeamId" to teamId,
            "status" to "ACTIVE"
        )

        firestore
            .collection(contestsCollection)
            .document(contest.contestId)
            .update(updates)
            .addOnSuccessListener {
                val updatedContest = contest.copy(
                    user2Id = userId,
                    user2Name = userName,
                    user2TeamId = teamId,
                    status = "ACTIVE"
                )
                onSuccess(updatedContest)
            }
            .addOnFailureListener {
                onFailure("Failed to join contest.")
            }
    }
}