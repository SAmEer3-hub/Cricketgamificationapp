package com.example.fantasycricketapp.ui.league



import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.ListenerRegistration
import com.example.fantasycricketapp.adapters.ChatAdapter
import com.example.fantasycricketapp.adapters.LeagueMemberAdapter
import com.example.fantasycricketapp.data.model.League
import com.example.fantasycricketapp.data.repository.LeagueRepository
import com.example.fantasycricketapp.databinding.ActivityLeagueDetailBinding
import com.example.fantasycricketapp.ui.team.TeamSelectionActivity
import com.example.fantasycricketapp.utils.FirebaseHelper

/**
 * LeagueDetailActivity — The heart of the Private League experience.
 *
 * This screen shows:
 * 1. The invite code (shareable)
 * 2. Live status banner (OPEN / ACTIVE / COMPLETED)
 * 3. Leaderboard — shows both members with scores after COMPLETED
 * 4. Action buttons — Select Team, Calculate Scores
 * 5. Real-time chat — messages appear instantly via Firestore listener
 *
 * Uses TWO real-time Firestore listeners:
 *   - leagueListener   → updates leaderboard when opponent joins/team submitted
 *   - messagesListener → updates chat instantly
 *
 * Both listeners are removed in onStop() to prevent memory leaks.
 *
 * Intent Extras required:
 *   "LEAGUE_CODE" → String
 *   "MATCH_ID"    → String
 *   "MATCH_NAME"  → String
 */
class LeagueDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLeagueDetailBinding
    private lateinit var leagueMemberAdapter: LeagueMemberAdapter
    private lateinit var chatAdapter: ChatAdapter

    private val leagueRepository = LeagueRepository()
    private val currentUserId    = FirebaseHelper.getCurrentUserId()

    // Intent extras
    private var leagueCode = ""
    private var matchId    = ""
    private var matchName  = ""

    // Current league state (updated by real-time listener)
    private var currentLeague: League? = null

    // Firestore real-time listeners — must be removed on stop
    private var leagueListener: ListenerRegistration? = null
    private var messagesListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeagueDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        leagueCode = intent.getStringExtra("LEAGUE_CODE") ?: ""
        matchId    = intent.getStringExtra("MATCH_ID")    ?: ""
        matchName  = intent.getStringExtra("MATCH_NAME")  ?: ""

        setupToolbar()
        setupLeaderboard()
        setupChat()
        setupButtons()
    }

    // ----------------------------------------------------------
    // Attach listeners when Activity becomes visible
    // ----------------------------------------------------------
    override fun onStart() {
        super.onStart()
        attachLeagueListener()
        attachChatListener()
    }

    // ----------------------------------------------------------
    // Detach listeners when Activity is hidden — CRITICAL
    // ----------------------------------------------------------
    override fun onStop() {
        super.onStop()
        leagueListener?.remove()
        messagesListener?.remove()
    }

    // ----------------------------------------------------------
    // Toolbar
    // ----------------------------------------------------------
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.tvDetailMatchName.text = matchName
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Share code from toolbar
        binding.tvShareCode.setOnClickListener {
            currentLeague?.let { shareCode(it) }
        }
    }

    // ----------------------------------------------------------
    // Leaderboard RecyclerView
    // ----------------------------------------------------------
    private fun setupLeaderboard() {
        leagueMemberAdapter = LeagueMemberAdapter(isCompleted = false)
        binding.rvLeaderboard.apply {
            layoutManager = LinearLayoutManager(this@LeagueDetailActivity)
            adapter = leagueMemberAdapter
            isNestedScrollingEnabled = false
        }
    }

    // ----------------------------------------------------------
    // Chat RecyclerView
    // ----------------------------------------------------------
    private fun setupChat() {
        chatAdapter = ChatAdapter()
        binding.rvChat.apply {
            layoutManager = LinearLayoutManager(this@LeagueDetailActivity).also {
                it.stackFromEnd = true   // New messages push from bottom
            }
            adapter = chatAdapter
        }
    }

    // ----------------------------------------------------------
    // Action buttons
    // ----------------------------------------------------------
    private fun setupButtons() {

        // Select team button → goes to TeamSelectionActivity
        binding.btnSelectTeam.setOnClickListener {
            val intent = Intent(this, TeamSelectionActivity::class.java)
            intent.putExtra("MATCH_ID",    matchId)
            intent.putExtra("MATCH_NAME",  matchName)
            intent.putExtra("LEAGUE_CODE", leagueCode) // Pass so team selection can update league
            startActivity(intent)
        }

        // Calculate scores button
        binding.btnCalculateScores.setOnClickListener {
            handleCalculateScores()
        }

        // Send chat message
        binding.btnSendMessage.setOnClickListener {
            sendChatMessage()
        }

        // Send on keyboard "Done"
        binding.etChatInput.setOnEditorActionListener { _, _, _ ->
            sendChatMessage()
            true
        }
    }

    // ----------------------------------------------------------
    // Real-time league listener — updates leaderboard + status
    // ----------------------------------------------------------
    private fun attachLeagueListener() {
        if (leagueCode.isEmpty()) return

        leagueListener = leagueRepository.listenToLeague(
            leagueCode = leagueCode,
            onUpdate   = { league ->
                currentLeague = league
                updateLeagueUI(league)
            }
        )
    }

    /**
     * Called every time the league document changes in Firestore.
     * Updates: toolbar title, status banner, leaderboard, action buttons.
     */
    private fun updateLeagueUI(league: League) {
        // Toolbar
        binding.tvDetailLeagueName.text = league.leagueName
        binding.tvDetailCode.text       = league.leagueCode

        // Status banner
        val (statusText, statusColor) = when (league.status) {
            "OPEN"      -> "⏳ Waiting for opponent to join..." to "#9E6A03"
            "ACTIVE"    -> "✅ Both players joined! Select your team." to "#238636"
            "COMPLETED" -> "🏆 Match complete — scores calculated!" to "#1F6FEB"
            else        -> "" to "#21262D"
        }
        binding.tvDetailStatus.text = statusText
        binding.tvDetailStatus.setBackgroundColor(
            android.graphics.Color.parseColor(statusColor)
        )

        // Leaderboard — sort by score descending if completed
        val sortedMembers = if (league.status == "COMPLETED") {
            league.members.sortedByDescending { it.score }
        } else {
            league.members
        }

        // Re-create adapter with isCompleted flag
        val isCompleted = league.status == "COMPLETED"
        leagueMemberAdapter = LeagueMemberAdapter(isCompleted = isCompleted)
        binding.rvLeaderboard.adapter = leagueMemberAdapter
        leagueMemberAdapter.submitList(sortedMembers)

        // Show/hide action buttons based on state
        updateActionButtons(league)
    }

    /**
     * Shows or hides the "Select Team" and "Calculate Scores" buttons
     * based on current league state.
     */
    private fun updateActionButtons(league: League) {
        val myMember = league.members.find { it.userId == currentUserId }
        val allHaveTeams = league.members.size == 2 && league.members.all { it.hasTeam }

        when (league.status) {
            "OPEN" -> {
                // Waiting for opponent — show select team if user hasn't submitted yet
                binding.btnSelectTeam.visibility =
                    if (myMember?.hasTeam == true) View.GONE else View.VISIBLE
                binding.btnCalculateScores.visibility = View.GONE
            }
            "ACTIVE" -> {
                // Both joined
                binding.btnSelectTeam.visibility =
                    if (myMember?.hasTeam == true) View.GONE else View.VISIBLE
                // Show calculate button only if BOTH have submitted teams
                binding.btnCalculateScores.visibility =
                    if (allHaveTeams) View.VISIBLE else View.GONE
            }
            "COMPLETED" -> {
                // Hide both action buttons
                binding.btnSelectTeam.visibility = View.GONE
                binding.btnCalculateScores.visibility = View.GONE
            }
        }
    }

    // ----------------------------------------------------------
    // Real-time chat listener
    // ----------------------------------------------------------
    private fun attachChatListener() {
        if (leagueCode.isEmpty()) return

        messagesListener = leagueRepository.listenToMessages(
            leagueCode = leagueCode,
            onUpdate   = { messages ->
                chatAdapter.submitList(messages)
                // Auto-scroll to the latest message
                if (messages.isNotEmpty()) {
                    binding.rvChat.post {
                        binding.rvChat.smoothScrollToPosition(messages.size - 1)
                    }
                }
            }
        )
    }

    // ----------------------------------------------------------
    // Send chat message
    // ----------------------------------------------------------
    private fun sendChatMessage() {
        val text = binding.etChatInput.text.toString().trim()
        if (text.isEmpty()) return

        // Clear input immediately for good UX
        binding.etChatInput.setText("")

        leagueRepository.sendMessage(
            leagueCode = leagueCode,
            text       = text,
            onSuccess  = { /* Message appears via real-time listener */ },
            onFailure  = { error ->
                Toast.makeText(this, "Send failed: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // ----------------------------------------------------------
    // Calculate scores
    // ----------------------------------------------------------
    private fun handleCalculateScores() {
        val league = currentLeague ?: return

        binding.progressDetail.visibility = View.VISIBLE
        binding.tvDetailMsg.visibility    = View.GONE
        binding.btnCalculateScores.isEnabled = false
        binding.btnCalculateScores.text   = "Calculating..."

        leagueRepository.calculateLeagueScores(
            league    = league,
            onSuccess = { updatedLeague ->
                binding.progressDetail.visibility = View.GONE
                binding.btnCalculateScores.visibility = View.GONE

                // Find winner name
                val winnerName = updatedLeague.members
                    .find { it.userId == updatedLeague.winnerId }?.userName
                    ?: if (updatedLeague.winnerId == "TIE") "TIE" else "Unknown"

                val msg = if (updatedLeague.winnerId == "TIE") {
                    "🤝 It's a tie! Both teams scored equally."
                } else {
                    "🏆 $winnerName wins the league!"
                }

                binding.tvDetailMsg.text = msg
                binding.tvDetailMsg.visibility = View.VISIBLE

                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                // Real-time listener will update leaderboard automatically
            },
            onFailure = { error ->
                binding.progressDetail.visibility = View.GONE
                binding.btnCalculateScores.isEnabled = true
                binding.btnCalculateScores.text = "⚡ Calculate Final Scores"
                Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
            }
        )
    }

    // ----------------------------------------------------------
    // Share code
    // ----------------------------------------------------------
    private fun shareCode(league: League) {
        val shareText = buildString {
            appendLine("🏏 Join my Fantasy Cricket league!")
            appendLine("League: ${league.leagueName}")
            appendLine("Match: ${league.matchName}")
            appendLine()
            appendLine("Invite Code: ${league.leagueCode}")
            appendLine()
            appendLine("Open the app → Leagues → Join with code")
        }
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(shareIntent, "Share via..."))
    }
}