package com.example.fantasycricketapp.ui.dashboard



import android.content.Intent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fantasycricketapp.adapters.MatchAdapter
import com.example.fantasycricketapp.data.model.Match
import com.example.fantasycricketapp.data.repository.AuthRepository
import com.example.fantasycricketapp.databinding.ActivityMainBinding
import com.example.fantasycricketapp.ui.auth.LoginActivity
import com.example.fantasycricketapp.ui.league.LeagueActivity
import com.example.fantasycricketapp.ui.profile.UserProfileActivity
import com.example.fantasycricketapp.ui.team.TeamSelectionActivity
import com.example.fantasycricketapp.utils.ApiResult
import com.example.fantasycricketapp.utils.FirebaseHelper
import java.util.Calendar

/**
 * MainActivity — Dashboard with real cricket matches from CricAPI.
 *
 * Changes from Phase 3:
 * - Now uses MatchViewModel to load real API data
 * - Observes LiveData instead of directly calling PlayerDummyData
 * - Shows loading spinner while API call is in progress
 * - Shows error message if API fails (fallback is handled in repository)
 * - Pull-to-refresh not needed since ViewModel caches data
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val authRepository = AuthRepository()
    private lateinit var matchAdapter: MatchAdapter

    // ViewModel — survives screen rotation
    private val viewModel: MatchViewModel by viewModels()

    private var selectedTab = "ALL"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!authRepository.isUserLoggedIn()) {
            navigateToLogin()
            return
        }

        setupToolbar()
        setupUserGreeting()
        setupRecyclerView()
        setupFilterTabs()
        setupFabLogout()
        setupBottomNavigation()
        observeMatches()
    }

    // ----------------------------------------------------------
    // Observe ViewModel LiveData
    // ----------------------------------------------------------

    /**
     * Observes the matchesState LiveData from MatchViewModel.
     * Reacts to Loading, Success, and Error states.
     */
    private fun observeMatches() {
        viewModel.matchesState.observe(this) { result ->
            when (result) {

                is ApiResult.Loading -> {
                    showLoadingState(true)
                }

                is ApiResult.Success -> {
                    showLoadingState(false)
                    binding.tvMatchesCount.text = result.data.size.toString()
                    // Apply current tab filter to the newly loaded data
                    applyCurrentFilter()
                }

                is ApiResult.Error -> {
                    // This shouldn't happen since repository always falls back
                    // but handle it gracefully just in case
                    showLoadingState(false)
                    Toast.makeText(
                        this,
                        "Using offline data: ${result.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    applyCurrentFilter()
                }
            }
        }
    }

    // ----------------------------------------------------------
    // Toolbar
    // ----------------------------------------------------------
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    // ----------------------------------------------------------
    // User greeting
    // ----------------------------------------------------------
    private fun setupUserGreeting() {
        val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..11  -> "Good Morning ☀️"
            in 12..17 -> "Good Afternoon 🌤️"
            else      -> "Good Evening 🌙"
        }
        binding.tvGreeting.text = greeting

        val displayName = FirebaseHelper.getCurrentUser()?.email?.substringBefore("@") ?: "Player"
        binding.tvUserName.text = displayName
        binding.tvMatchesCount.text = "..."   // Shows "..." while loading
    }

    // ----------------------------------------------------------
    // RecyclerView
    // ----------------------------------------------------------
    private fun setupRecyclerView() {
        matchAdapter = MatchAdapter { match -> onMatchPlayClicked(match) }
        binding.rvMatches.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = matchAdapter
            isNestedScrollingEnabled = false
        }
    }

    // ----------------------------------------------------------
    // Filter tabs
    // ----------------------------------------------------------
    private fun setupFilterTabs() {
        binding.tabAll.setOnClickListener       { filterMatches("ALL") }
        binding.tabLive.setOnClickListener      { filterMatches("LIVE") }
        binding.tabUpcoming.setOnClickListener  { filterMatches("UPCOMING") }
        binding.tabCompleted.setOnClickListener { filterMatches("COMPLETED") }
    }

    private fun filterMatches(filter: String) {
        selectedTab = filter
        updateTabUI(filter)
        applyCurrentFilter()
    }

    /**
     * Applies the current selectedTab filter to the ViewModel's loaded data.
     * Called after data loads AND when user taps a tab.
     */
    private fun applyCurrentFilter() {
        val filtered = viewModel.filterMatches(selectedTab)

        binding.tvSectionTitle.text = when (selectedTab) {
            "LIVE"      -> "🔴 Live Matches (${filtered.size})"
            "UPCOMING"  -> "Upcoming Matches (${filtered.size})"
            "COMPLETED" -> "Completed Matches (${filtered.size})"
            else        -> "All Matches (${filtered.size})"
        }

        matchAdapter.submitList(filtered)

        // Show empty state if no matches for this filter
        if (filtered.isEmpty() && viewModel.getAllMatches().isNotEmpty()) {
            Toast.makeText(
                this,
                "No ${selectedTab.lowercase()} matches right now",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateTabUI(selected: String) {
        listOf(binding.tabAll, binding.tabLive, binding.tabUpcoming, binding.tabCompleted)
            .forEach { tab ->
                tab.setBackgroundResource(
                    com.example.fantasycricketapp.R.drawable.bg_tab_unselected
                )
                tab.setTextColor(android.graphics.Color.parseColor("#8B949E"))
            }

        val activeTab: TextView = when (selected) {
            "LIVE"      -> binding.tabLive
            "UPCOMING"  -> binding.tabUpcoming
            "COMPLETED" -> binding.tabCompleted
            else        -> binding.tabAll
        }
        activeTab.setBackgroundResource(
            com.example.fantasycricketapp.R.drawable.bg_tab_selected
        )
        activeTab.setTextColor(android.graphics.Color.WHITE)
    }

    // ----------------------------------------------------------
    // Loading state UI
    // ----------------------------------------------------------
    private fun showLoadingState(isLoading: Boolean) {
        // Show/hide progress bar over the RecyclerView
        // You can add a ProgressBar to activity_main.xml if desired
        // For now just update the section title
        if (isLoading) {
            binding.tvSectionTitle.text = "Loading matches... 🔄"
        }
    }

    // ----------------------------------------------------------
    // Match play button clicked
    // ----------------------------------------------------------
    private fun onMatchPlayClicked(match: Match) {
        when (match.status) {
            "COMPLETED" -> Toast.makeText(
                this, "This match has ended.", Toast.LENGTH_SHORT
            ).show()
            else -> {
                val intent = Intent(this, TeamSelectionActivity::class.java)
                intent.putExtra("MATCH_ID",   match.matchId)
                intent.putExtra("MATCH_NAME", match.matchName)
                startActivity(intent)
            }
        }
    }

    // ----------------------------------------------------------
    // Bottom navigation
    // ----------------------------------------------------------
    private fun setupBottomNavigation() {
        binding.btnNavLeagues.setOnClickListener {
            startActivity(Intent(this, LeagueActivity::class.java))
        }
        binding.btnNavProfile.setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
        }
    }

    // ----------------------------------------------------------
    // Logout
    // ----------------------------------------------------------
    private fun setupFabLogout() {
        binding.fabLogout.setOnClickListener { showLogoutDialog() }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes, Logout") { _, _ ->
                authRepository.logout()
                navigateToLogin()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun navigateToLogin() {
        startActivity(
            Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        finish()
    }

    // ----------------------------------------------------------
    // Pull to refresh — user can force refresh from toolbar
    // ----------------------------------------------------------
    override fun onResume() {
        super.onResume()
        // If data is already loaded, just re-apply the filter
        // Don't re-fetch unless cache is stale (handled inside ViewModel)
        if (viewModel.getAllMatches().isNotEmpty()) {
            applyCurrentFilter()
        }
    }
}