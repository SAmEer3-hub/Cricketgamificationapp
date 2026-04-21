package com.example.fantasycricketapp.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

/**
 * FirebaseHelper — Updated with new Phase 7 collection constants.
 */
object FirebaseHelper {

    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    // ── Phase 1–6 collections ──
    const val COLLECTION_USERS    = "users"
    const val COLLECTION_MATCHES  = "matches"
    const val COLLECTION_TEAMS    = "teams"
    const val COLLECTION_CONTESTS = "contests"
    const val COLLECTION_POINTS   = "points"

    // ── Phase 7 collections ──
    const val COLLECTION_LEAGUES  = "leagues"          // Private league documents
    const val SUBCOLLECTION_CHAT  = "messages"         // Subcollection under each league

    fun getCurrentUser(): FirebaseUser? = auth.currentUser
    fun getCurrentUserId(): String = auth.currentUser?.uid ?: ""
    fun getCurrentUserEmail(): String = auth.currentUser?.email ?: ""
    fun getCurrentUserName(): String =
        auth.currentUser?.email?.substringBefore("@") ?: "Player"

    fun isLoggedIn(): Boolean = auth.currentUser != null
}