package com.example.fantasycricketapp.ui.league



import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fantasycricketapp.adapters.LeagueAdapter
import com.example.fantasycricketapp.data.model.League
import com.example.fantasycricketapp.data.repository.LeagueRepository
import com.example.fantasycricketapp.databinding.ActivityLeagueBinding

/**
 * LeagueActivity — Entry point for the Private League system.
 *
 * This screen allows users to:
 * 1. CREATE a new private league (gets an invite code)
 * 2. JOIN an existing league by entering a friend's code
 * 3. VIEW all leagues they are part of
 *
 * Intent Extras (optional — passed from Dashboard when "Play" is tapped):
 *   "MATCH_ID"   → pre-fills the selected match for creation
 *   "MATCH_NAME" → display name of the pre-selected match
 */
class LeagueActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLeagueBinding
    private lateinit var leagueAdapter: LeagueAdapter
    private val leagueRepository = LeagueRepository()

    // Pre-selected match from Dashboard (optional)
    private var matchId   = ""
    private var matchName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeagueBinding.inflate(layoutInflater)
        setContentView(binding.root)

        matchId   = intent.getStringExtra("MATCH_ID")   ?: ""
        matchName = intent.getStringExtra("MATCH_NAME") ?: ""

        setupToolbar()
        setupMatchDisplay()
        setupRecyclerView()
        setupCreateButton()
        setupJoinButton()
        setupRefresh()
        loadMyLeagues()
    }

    // ----------------------------------------------------------
    // Toolbar
    // ----------------------------------------------------------
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    // ----------------------------------------------------------
    // Show pre-selected match in Create section
    // ----------------------------------------------------------
    private fun setupMatchDisplay() {
        if (matchName.isNotEmpty()) {
            binding.tvCreateSelectedMatch.text = "🏏 $matchName"
            binding.tvCreateSelectedMatch.setTextColor(
                android.graphics.Color.parseColor("#FFFFFF")
            )
        }
    }

    // ----------------------------------------------------------
    // RecyclerView
    // ----------------------------------------------------------
    private fun setupRecyclerView() {
        leagueAdapter = LeagueAdapter(
            onOpenClick  = { league -> openLeagueDetail(league) },
            onShareClick = { league -> shareLeagueCode(league) }
        )
        binding.rvLeagues.apply {
            layoutManager = LinearLayoutManager(this@LeagueActivity)
            adapter = leagueAdapter
            isNestedScrollingEnabled = false
        }
    }

    // ----------------------------------------------------------
    // CREATE button
    // ----------------------------------------------------------
    private fun setupCreateButton() {
        binding.btnCreateLeague.setOnClickListener {
            handleCreateLeague()
        }
    }

    private fun handleCreateLeague() {
        val leagueName = binding.etLeagueName.text.toString().trim()

        if (matchId.isEmpty()) {
            showCreateError("Please go back and tap 'Play Now' on a match first.")
            return
        }

        showCreateLoading(true)
        clearCreateMessages()

        leagueRepository.createLeague(
            leagueName = leagueName,
            matchId    = matchId,
            matchName  = matchName,
            onSuccess  = { league ->
                showCreateLoading(false)
                showCreateSuccess(
                    "🎉 League created!\nYour invite code: ${league.leagueCode}\n" +
                            "Share this code with your friend!"
                )
                loadMyLeagues()

                // Auto-offer to share
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    shareLeagueCode(league)
                }, 800)
            },
            onFailure = { error ->
                showCreateLoading(false)
                showCreateError(error)
            }
        )
    }

    // ----------------------------------------------------------
    // JOIN button
    // ----------------------------------------------------------
    private fun setupJoinButton() {
        binding.btnJoinLeague.setOnClickListener {
            handleJoinLeague()
        }
    }

    private fun handleJoinLeague() {
        val code = binding.etJoinCode.text.toString().trim().uppercase()

        if (code.isEmpty()) {
            showJoinError("Please enter an invite code.")
            return
        }

        showJoinLoading(true)
        clearJoinMessages()

        leagueRepository.joinLeague(
            rawCode         = code,
            onSuccess       = { league ->
                showJoinLoading(false)
                showJoinSuccess("✅ Joined '${league.leagueName}'! Opening league...")
                loadMyLeagues()
                // Navigate to league detail after short delay
                binding.root.postDelayed({ openLeagueDetail(league) }, 1000)
            },
            onAlreadyMember = {
                showJoinLoading(false)
                showJoinError("You are already a member of this league.")
            },
            onNotFound = {
                showJoinLoading(false)
                showJoinError("No league found with code '$code'. Check the code and try again.")
            },
            onFull = {
                showJoinLoading(false)
                showJoinError("This league is already full (2/2 members).")
            },
            onCompleted = {
                showJoinLoading(false)
                showJoinError("This league has already finished. You can't join a completed league.")
            },
            onOwnLeague = {
                showJoinLoading(false)
                showJoinError("You can't join your own league! Share the code with a friend.")
            },
            onFailure = { error ->
                showJoinLoading(false)
                showJoinError(error)
            }
        )
    }

    // ----------------------------------------------------------
    // Share invite code via Android share sheet
    // ----------------------------------------------------------
    private fun shareLeagueCode(league: League) {
        val shareText = buildString {
            appendLine("🏏 Join my Fantasy Cricket league!")
            appendLine()
            appendLine("Match: ${league.matchName}")
            appendLine("League: ${league.leagueName}")
            appendLine()
            appendLine("Your invite code: ${league.leagueCode}")
            appendLine()
            appendLine("Open Fantasy Cricket App → Leagues → Join with code")
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "Fantasy Cricket League Invite")
        }

        startActivity(
            Intent.createChooser(shareIntent, "Share invite code via...")
        )
    }

    // ----------------------------------------------------------
    // Open league detail screen
    // ----------------------------------------------------------
    private fun openLeagueDetail(league: League) {
        val intent = Intent(this, LeagueDetailActivity::class.java)
        intent.putExtra("LEAGUE_CODE", league.leagueCode)
        intent.putExtra("MATCH_ID",    league.matchId)
        intent.putExtra("MATCH_NAME",  league.matchName)
        startActivity(intent)
    }

    // ----------------------------------------------------------
    // Load my leagues
    // ----------------------------------------------------------
    private fun loadMyLeagues() {
        binding.progressLeaguesList.visibility = View.VISIBLE
        binding.tvNoLeagues.visibility = View.GONE

        leagueRepository.fetchMyLeagues(
            onSuccess = { leagues ->
                binding.progressLeaguesList.visibility = View.GONE
                if (leagues.isEmpty()) {
                    binding.tvNoLeagues.visibility = View.VISIBLE
                    binding.rvLeagues.visibility = View.GONE
                } else {
                    binding.tvNoLeagues.visibility = View.GONE
                    binding.rvLeagues.visibility = View.VISIBLE
                    leagueAdapter.submitList(leagues)
                }
            },
            onFailure = { error ->
                binding.progressLeaguesList.visibility = View.GONE
                binding.tvNoLeagues.visibility = View.VISIBLE
                android.util.Log.e("LeagueActivity", "Load leagues error: $error")
            }
        )
    }

    // ----------------------------------------------------------
    // Refresh
    // ----------------------------------------------------------
    private fun setupRefresh() {
        binding.tvRefreshLeagues.setOnClickListener {
            loadMyLeagues()
            Toast.makeText(this, "Refreshing...", Toast.LENGTH_SHORT).show()
        }
    }

    // ----------------------------------------------------------
    // UI helpers
    // ----------------------------------------------------------
    private fun showCreateLoading(isLoading: Boolean) {
        binding.progressCreate.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnCreateLeague.isEnabled = !isLoading
        binding.btnCreateLeague.text =
            if (isLoading) "Creating..." else "Create League & Get Invite Code"
    }

    private fun showCreateSuccess(msg: String) {
        binding.tvCreateSuccess.text = msg
        binding.tvCreateSuccess.visibility = View.VISIBLE
        binding.tvCreateError.visibility = View.GONE
    }

    private fun showCreateError(msg: String) {
        binding.tvCreateError.text = msg
        binding.tvCreateError.visibility = View.VISIBLE
        binding.tvCreateSuccess.visibility = View.GONE
    }

    private fun clearCreateMessages() {
        binding.tvCreateSuccess.visibility = View.GONE
        binding.tvCreateError.visibility = View.GONE
    }

    private fun showJoinLoading(isLoading: Boolean) {
        binding.progressJoin.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnJoinLeague.isEnabled = !isLoading
        binding.btnJoinLeague.text = if (isLoading) "Joining..." else "Join League"
    }

    private fun showJoinSuccess(msg: String) {
        binding.tvJoinSuccess.text = msg
        binding.tvJoinSuccess.visibility = View.VISIBLE
        binding.tvJoinError.visibility = View.GONE
    }

    private fun showJoinError(msg: String) {
        binding.tvJoinError.text = msg
        binding.tvJoinError.visibility = View.VISIBLE
        binding.tvJoinSuccess.visibility = View.GONE
    }

    private fun clearJoinMessages() {
        binding.tvJoinSuccess.visibility = View.GONE
        binding.tvJoinError.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        loadMyLeagues() // Refresh when returning from detail screen
    }
}