package com.example.fantasycricketapp.ui.profile


import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.Query
import com.example.fantasycricketapp.data.model.User
import com.example.fantasycricketapp.databinding.ActivityUserProfileBinding
import com.example.fantasycricketapp.utils.FirebaseHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * UserProfileActivity — Displays the current user's stats and profile.
 *
 * Data loaded from Firestore:
 * 1. User document (name, email, createdAt)
 * 2. Contests where user is user1 → count total + wins
 * 3. Contests where user is user2 → count total + wins
 * 4. Leagues where user is creator
 *
 * Stats computed:
 * - Total contests = user1 contests + user2 contests
 * - Total wins     = contests where winnerId == userId
 * - Total losses   = total - wins - ties
 * - Win rate       = wins / total × 100
 */
class UserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserProfileBinding
    private val db        = FirebaseHelper.firestore
    private val currentUserId = FirebaseHelper.getCurrentUserId()
    private val dateFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadProfile()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    // ----------------------------------------------------------
    // Load profile data from Firestore
    // ----------------------------------------------------------
    private fun loadProfile() {
        binding.progressProfile.visibility = View.VISIBLE

        // Load User document
        db.collection(FirebaseHelper.COLLECTION_USERS)
            .document(currentUserId)
            .get()
            .addOnSuccessListener { doc ->
                val user = doc.toObject(User::class.java)
                if (user != null) {
                    displayUserInfo(user)
                } else {
                    // User doc might not exist — use Auth data
                    displayFallbackInfo()
                }
                // Load stats after getting user info
                loadContestStats()
                loadLeagueStats()
            }
            .addOnFailureListener {
                displayFallbackInfo()
                loadContestStats()
                loadLeagueStats()
            }
    }

    // ----------------------------------------------------------
    // Display user info section
    // ----------------------------------------------------------
    private fun displayUserInfo(user: User) {
        val displayName = user.name.ifEmpty {
            FirebaseHelper.getCurrentUserName()
        }
        val email = user.email.ifEmpty {
            FirebaseHelper.getCurrentUserEmail()
        }

        binding.tvProfileName.text  = displayName
        binding.tvProfileEmail.text = email
        binding.tvProfileAvatar.text =
            displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "P"

        if (user.createdAt > 0) {
            binding.tvMemberSince.text =
                "Member since ${dateFormat.format(Date(user.createdAt))}"
        }
    }

    private fun displayFallbackInfo() {
        val name  = FirebaseHelper.getCurrentUserName()
        val email = FirebaseHelper.getCurrentUserEmail()

        binding.tvProfileName.text  = name
        binding.tvProfileEmail.text = email
        binding.tvProfileAvatar.text =
            name.firstOrNull()?.uppercaseChar()?.toString() ?: "P"
    }

    // ----------------------------------------------------------
    // Load contest stats — two parallel queries
    // ----------------------------------------------------------
    private fun loadContestStats() {
        var totalContests = 0
        var totalWins     = 0
        var queriesDone   = 0

        fun finalizeStats() {
            queriesDone++
            if (queriesDone < 2) return

            val losses  = totalContests - totalWins
            val winRate = if (totalContests > 0) (totalWins * 100) / totalContests else 0

            runOnUiThread {
                binding.progressProfile.visibility = View.GONE
                binding.tvStatContests.text = "$totalContests"
                binding.tvStatWins.text     = "$totalWins"
                binding.tvStatLosses.text   = "$losses"
                binding.tvWinRate.text      = "$winRate%"
                binding.progressWinRate.progress = winRate
            }
        }

        // Query 1: contests where user is player 1
        db.collection(FirebaseHelper.COLLECTION_CONTESTS)
            .whereEqualTo("user1Id", currentUserId)
            .whereEqualTo("status", "COMPLETED")
            .get()
            .addOnSuccessListener { snap ->
                val contests = snap.documents
                totalContests += contests.size
                totalWins += contests.count { doc ->
                    doc.getString("winnerId") == currentUserId
                }
                finalizeStats()
            }
            .addOnFailureListener { finalizeStats() }

        // Query 2: contests where user is player 2
        db.collection(FirebaseHelper.COLLECTION_CONTESTS)
            .whereEqualTo("user2Id", currentUserId)
            .whereEqualTo("status", "COMPLETED")
            .get()
            .addOnSuccessListener { snap ->
                val contests = snap.documents
                totalContests += contests.size
                totalWins += contests.count { doc ->
                    doc.getString("winnerId") == currentUserId
                }
                finalizeStats()
            }
            .addOnFailureListener { finalizeStats() }
    }

    // ----------------------------------------------------------
    // Load league stats
    // ----------------------------------------------------------
    private fun loadLeagueStats() {
        db.collection(FirebaseHelper.COLLECTION_LEAGUES)
            .whereEqualTo("creatorId", currentUserId)
            .get()
            .addOnSuccessListener { snap ->
                val leagueCount = snap.size()
                binding.tvLeaguesStat.text = "$leagueCount league${if (leagueCount != 1) "s" else ""} joined"
            }
            .addOnFailureListener {
                binding.tvLeaguesStat.text = "Could not load leagues"
            }
    }
}