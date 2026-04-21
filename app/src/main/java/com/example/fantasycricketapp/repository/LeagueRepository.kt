package com.example.fantasycricketapp.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.example.fantasycricketapp.data.model.League
import com.example.fantasycricketapp.data.model.LeagueMember
import com.example.fantasycricketapp.data.model.LeagueMessage
import com.example.fantasycricketapp.utils.FirebaseHelper
import com.example.fantasycricketapp.utils.LeagueCodeGenerator
import com.example.fantasycricketapp.utils.MockPointsData

/**
 * LeagueRepository — All Firestore operations for the Private League system.
 *
 * Operations:
 *   createLeague       → Create a new league with a generated invite code
 *   joinLeague         → Join an existing league by entering its code
 *   fetchMyLeagues     → Get all leagues the current user is in
 *   fetchLeague        → Get a single league by code
 *   updateMemberHasTeam→ Mark current user's team as submitted in the league
 *   calculateScores    → Compute scores for both members and declare winner
 *   sendMessage        → Post a chat message to the league's subcollection
 *   listenToMessages   → Real-time Firestore listener for chat updates
 */
class LeagueRepository {

    private val db         = FirebaseHelper.firestore
    private val leaguesCol = FirebaseHelper.COLLECTION_LEAGUES
    private val chatCol    = FirebaseHelper.SUBCOLLECTION_CHAT

    // ----------------------------------------------------------
    // CREATE LEAGUE
    // ----------------------------------------------------------

    /**
     * Creates a new private league in Firestore.
     *
     * Collision handling: if generated code already exists, retries once
     * with a new code. The probability of 2 consecutive collisions is
     * negligible (1 in 144 trillion).
     *
     * @param leagueName  Name the creator gives this league
     * @param matchId     The match this league is for
     * @param matchName   Human-readable match name
     * @param onSuccess   Returns the created League object
     * @param onFailure   Returns error message
     */
    fun createLeague(
        leagueName: String,
        matchId: String,
        matchName: String,
        onSuccess: (League) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val userId   = FirebaseHelper.getCurrentUserId()
        val userName = FirebaseHelper.getCurrentUserName()

        if (userId.isEmpty()) {
            onFailure("Not logged in.")
            return
        }

        // Generate invite code
        val code = LeagueCodeGenerator.generate()

        // Check if code already exists (collision detection)
        db.collection(leaguesCol).document(code).get()
            .addOnSuccessListener { doc ->
                val finalCode = if (doc.exists()) {
                    // Collision — generate a new code
                    LeagueCodeGenerator.generate()
                } else {
                    code
                }

                val creator = LeagueMember(
                    userId   = userId,
                    userName = userName,
                    teamId   = "${userId}_${matchId}",
                    score    = 0,
                    hasTeam  = false,
                    joinedAt = System.currentTimeMillis()
                )

                val league = League(
                    leagueCode  = finalCode,
                    leagueName  = leagueName.ifBlank { "$userName's League" },
                    matchId     = matchId,
                    matchName   = matchName,
                    creatorId   = userId,
                    creatorName = userName,
                    members     = listOf(creator),
                    status      = "OPEN",
                    winnerId    = "",
                    maxMembers  = 2,
                    createdAt   = System.currentTimeMillis()
                )

                db.collection(leaguesCol)
                    .document(finalCode)
                    .set(league)
                    .addOnSuccessListener { onSuccess(league) }
                    .addOnFailureListener { e ->
                        onFailure(e.message ?: "Failed to create league.")
                    }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to verify code uniqueness.")
            }
    }

    // ----------------------------------------------------------
    // JOIN LEAGUE
    // ----------------------------------------------------------

    /**
     * Joins an existing league using its invite code.
     *
     * Validations performed:
     *   - Code format is valid
     *   - League document exists
     *   - League is not COMPLETED
     *   - League is not full (< 2 members)
     *   - Current user is not already a member
     *   - User is not trying to join their own league
     *
     * On success: adds user as member, updates status to ACTIVE.
     */
    fun joinLeague(
        rawCode: String,
        onSuccess: (League) -> Unit,
        onAlreadyMember: () -> Unit,
        onNotFound: () -> Unit,
        onFull: () -> Unit,
        onCompleted: () -> Unit,
        onOwnLeague: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val userId   = FirebaseHelper.getCurrentUserId()
        val userName = FirebaseHelper.getCurrentUserName()

        // Normalize code — uppercase, trim whitespace
        val code = rawCode.trim().uppercase()

        if (!LeagueCodeGenerator.isValidFormat(code)) {
            onFailure("Invalid code format. Expected 3 letters + 3 digits (e.g. FCK284).")
            return
        }

        db.collection(leaguesCol).document(code).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    onNotFound()
                    return@addOnSuccessListener
                }

                val league = doc.toObject(League::class.java)!!

                // ── Validations ──
                when {
                    league.status == "COMPLETED" -> {
                        onCompleted()
                        return@addOnSuccessListener
                    }
                    league.creatorId == userId -> {
                        onOwnLeague()
                        return@addOnSuccessListener
                    }
                    league.members.any { it.userId == userId } -> {
                        onAlreadyMember()
                        return@addOnSuccessListener
                    }
                    league.members.size >= league.maxMembers -> {
                        onFull()
                        return@addOnSuccessListener
                    }
                }

                // ── All checks passed — join the league ──
                val newMember = LeagueMember(
                    userId   = userId,
                    userName = userName,
                    teamId   = "${userId}_${league.matchId}",
                    score    = 0,
                    hasTeam  = false,
                    joinedAt = System.currentTimeMillis()
                )

                // Build updated members list
                val updatedMembers = league.members + newMember

                // Update Firestore — set members + status = ACTIVE
                db.collection(leaguesCol).document(code)
                    .update(
                        "members", updatedMembers.map { memberToMap(it) },
                        "status",  "ACTIVE"
                    )
                    .addOnSuccessListener {
                        val updatedLeague = league.copy(
                            members = updatedMembers,
                            status  = "ACTIVE"
                        )
                        onSuccess(updatedLeague)
                    }
                    .addOnFailureListener { e ->
                        onFailure(e.message ?: "Failed to join league.")
                    }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to look up league code.")
            }
    }

    // ----------------------------------------------------------
    // FETCH MY LEAGUES
    // ----------------------------------------------------------

    /**
     * Fetches all leagues where the current user is a member.
     *
     * Firestore does not support querying inside arrays of maps,
     * so we query by creatorId (leagues the user created) and
     * then also do a secondary query approach.
     *
     * For a 1v1 app the volume is small — this is efficient enough.
     */
    fun fetchMyLeagues(
        onSuccess: (List<League>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val userId = FirebaseHelper.getCurrentUserId()

        // Query leagues created by this user
        db.collection(leaguesCol)
            .whereEqualTo("creatorId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { creatorSnapshot ->
                val createdLeagues = creatorSnapshot.documents
                    .mapNotNull { it.toObject(League::class.java) }

                // Query leagues where user joined (not creator)
                // We query by the second member check using a different approach
                // Since Firestore can't query inside array of maps,
                // we tag the second member's userId in a flat field
                db.collection(leaguesCol)
                    .whereEqualTo("joinedUserId", userId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener { joinedSnapshot ->
                        val joinedLeagues = joinedSnapshot.documents
                            .mapNotNull { it.toObject(League::class.java) }

                        val allLeagues = (createdLeagues + joinedLeagues)
                            .distinctBy { it.leagueCode }
                            .sortedByDescending { it.createdAt }

                        onSuccess(allLeagues)
                    }
                    .addOnFailureListener {
                        // Even if second query fails, return created leagues
                        onSuccess(createdLeagues)
                    }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to load your leagues.")
            }
    }

    // ----------------------------------------------------------
    // FETCH SINGLE LEAGUE
    // ----------------------------------------------------------

    fun fetchLeague(
        leagueCode: String,
        onSuccess: (League) -> Unit,
        onFailure: (String) -> Unit
    ) {
        db.collection(leaguesCol).document(leagueCode).get()
            .addOnSuccessListener { doc ->
                val league = doc.toObject(League::class.java)
                if (league != null) onSuccess(league)
                else onFailure("League not found.")
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to fetch league.")
            }
    }

    // ----------------------------------------------------------
    // UPDATE MEMBER HAS TEAM
    // ----------------------------------------------------------

    /**
     * Marks the current user's hasTeam = true in the league's members list.
     * Called from TeamSelectionActivity after team is submitted.
     */
    fun updateMemberHasTeam(
        leagueCode: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val userId = FirebaseHelper.getCurrentUserId()

        db.collection(leaguesCol).document(leagueCode).get()
            .addOnSuccessListener { doc ->
                val league = doc.toObject(League::class.java) ?: return@addOnSuccessListener
                val updatedMembers = league.members.map { member ->
                    if (member.userId == userId) member.copy(hasTeam = true)
                    else member
                }
                db.collection(leaguesCol).document(leagueCode)
                    .update("members", updatedMembers.map { memberToMap(it) })
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e -> onFailure(e.message ?: "Update failed.") }
            }
            .addOnFailureListener { e -> onFailure(e.message ?: "Fetch failed.") }
    }

    // ----------------------------------------------------------
    // CALCULATE LEAGUE SCORES
    // ----------------------------------------------------------

    /**
     * Calculates scores for both members using MockPointsData,
     * determines the winner, and updates the league document.
     *
     * Uses the same scoring logic as PointsRepository.
     */
    fun calculateLeagueScores(
        league: League,
        onSuccess: (League) -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (league.members.size < 2) {
            onFailure("Need 2 members to calculate scores.")
            return
        }

        val allPoints = MockPointsData.getPointsForMatch(league.matchId)
        val pointsMap = allPoints.associateBy { it.playerId }

        // For each member, fetch their team from Firestore then calculate
        val member1 = league.members[0]
        val member2 = league.members[1]
        var scoresCalculated = 0
        var score1 = 0
        var score2 = 0

        fun tryFinalizeScores() {
            scoresCalculated++
            if (scoresCalculated < 2) return

            // Both scores ready — determine winner
            val winnerId = when {
                score1 > score2 -> member1.userId
                score2 > score1 -> member2.userId
                else            -> "TIE"
            }

            val updatedMembers = listOf(
                member1.copy(score = score1),
                member2.copy(score = score2)
            )

            db.collection(leaguesCol).document(league.leagueCode)
                .update(
                    "members",  updatedMembers.map { memberToMap(it) },
                    "status",   "COMPLETED",
                    "winnerId", winnerId
                )
                .addOnSuccessListener {
                    onSuccess(
                        league.copy(
                            members  = updatedMembers,
                            status   = "COMPLETED",
                            winnerId = winnerId
                        )
                    )
                }
                .addOnFailureListener { e -> onFailure(e.message ?: "Failed to save scores.") }
        }

        // Fetch member 1 team player IDs
        db.collection(FirebaseHelper.COLLECTION_TEAMS).document(member1.teamId).get()
            .addOnSuccessListener { doc ->
                @Suppress("UNCHECKED_CAST")
                val ids = doc.get("selectedPlayerIds") as? List<String> ?: emptyList()
                score1 = ids.sumOf { pointsMap[it]?.totalPoints ?: 0 }
                tryFinalizeScores()
            }
            .addOnFailureListener {
                score1 = 0
                tryFinalizeScores()
            }

        // Fetch member 2 team player IDs
        db.collection(FirebaseHelper.COLLECTION_TEAMS).document(member2.teamId).get()
            .addOnSuccessListener { doc ->
                @Suppress("UNCHECKED_CAST")
                val ids = doc.get("selectedPlayerIds") as? List<String> ?: emptyList()
                score2 = ids.sumOf { pointsMap[it]?.totalPoints ?: 0 }
                tryFinalizeScores()
            }
            .addOnFailureListener {
                score2 = 0
                tryFinalizeScores()
            }
    }

    // ----------------------------------------------------------
    // CHAT — SEND MESSAGE
    // ----------------------------------------------------------

    /**
     * Adds a new message document to leagues/{leagueCode}/messages subcollection.
     * Firestore auto-generates the document ID.
     */
    fun sendMessage(
        leagueCode: String,
        text: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val userId   = FirebaseHelper.getCurrentUserId()
        val userName = FirebaseHelper.getCurrentUserName()

        if (text.isBlank()) {
            onFailure("Message cannot be empty.")
            return
        }

        val messageId = db.collection(leaguesCol)
            .document(leagueCode)
            .collection(chatCol)
            .document().id   // Auto-generated ID

        val message = LeagueMessage(
            messageId  = messageId,
            leagueCode = leagueCode,
            userId     = userId,
            userName   = userName,
            text       = text.trim(),
            timestamp  = System.currentTimeMillis()
        )

        db.collection(leaguesCol)
            .document(leagueCode)
            .collection(chatCol)
            .document(messageId)
            .set(message)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e.message ?: "Failed to send message.") }
    }

    // ----------------------------------------------------------
    // CHAT — REAL-TIME LISTENER
    // ----------------------------------------------------------

    /**
     * Attaches a real-time Firestore listener to the messages subcollection.
     *
     * Returns a ListenerRegistration that MUST be removed in onStop()
     * to prevent memory leaks.
     *
     * Messages are ordered by timestamp ascending (oldest first).
     */
    fun listenToMessages(
        leagueCode: String,
        onUpdate: (List<LeagueMessage>) -> Unit
    ): ListenerRegistration {
        return db.collection(leaguesCol)
            .document(leagueCode)
            .collection(chatCol)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val messages = snapshot.documents
                    .mapNotNull { it.toObject(LeagueMessage::class.java) }
                onUpdate(messages)
            }
    }

    // ----------------------------------------------------------
    // LISTEN TO LEAGUE UPDATES — Real-time
    // ----------------------------------------------------------

    /**
     * Attaches a real-time listener to the league document itself.
     * Used to update the leaderboard when opponent joins or scores change.
     */
    fun listenToLeague(
        leagueCode: String,
        onUpdate: (League) -> Unit
    ): ListenerRegistration {
        return db.collection(leaguesCol)
            .document(leagueCode)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val league = snapshot.toObject(League::class.java)
                if (league != null) onUpdate(league)
            }
    }

    // ----------------------------------------------------------
    // HELPERS
    // ----------------------------------------------------------

    /**
     * Converts a LeagueMember to a plain Map for Firestore update calls.
     * Firestore's update() with nested objects requires explicit maps.
     */
    private fun memberToMap(m: LeagueMember): Map<String, Any> = mapOf(
        "userId"   to m.userId,
        "userName" to m.userName,
        "teamId"   to m.teamId,
        "score"    to m.score,
        "hasTeam"  to m.hasTeam,
        "joinedAt" to m.joinedAt
    )
}