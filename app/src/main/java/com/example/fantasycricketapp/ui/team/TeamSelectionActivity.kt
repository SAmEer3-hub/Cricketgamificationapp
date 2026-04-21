package com.example.fantasycricketapp.ui.team

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fantasycricketapp.adapters.PlayerAdapter
import com.example.fantasycricketapp.data.model.Player
import com.example.fantasycricketapp.databinding.ActivityTeamSelectionBinding
import com.example.fantasycricketapp.ui.contest.ContestActivity
import com.example.fantasycricketapp.utils.PlayerDummyData
import androidx.activity.viewModels
import com.example.fantasycricketapp.utils.ApiResult

class TeamSelectionActivity : AppCompatActivity() {

    private val teamViewModel: TeamViewModel by viewModels()
    private lateinit var binding: ActivityTeamSelectionBinding
    private lateinit var playerAdapter: PlayerAdapter

    private var matchId: String = ""
    private var matchName: String = ""

    private var allPlayers: List<Player> = emptyList()
    private var currentFilter: String = "ALL"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeamSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        matchId   = intent.getStringExtra("MATCH_ID") ?: ""
        matchName = intent.getStringExtra("MATCH_NAME") ?: "Match"

        setupToolbar()
        setupRecyclerView()
        loadPlayers()
        setupFilterTabs()
        setupSubmitButton()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.tvMatchNameToolbar.text = matchName

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        playerAdapter = PlayerAdapter { selectedCount ->
            updateSelectionUI(selectedCount)
        }

        binding.rvPlayers.apply {
            layoutManager = LinearLayoutManager(this@TeamSelectionActivity)
            adapter = playerAdapter
        }
    }

    // ----------------------------------------------------------
// Load players using ViewModel (real API data with fallback)
// ----------------------------------------------------------
    private fun loadPlayers() {
        // Show loading state
        binding.tvValidationMsg.text = "Loading players... 🔄"
        binding.tvValidationMsg.visibility = View.VISIBLE
        binding.btnSubmitTeam.isEnabled = false

        // Observe the squad state
        teamViewModel.squadState.observe(this) { result ->
            when (result) {
                is ApiResult.Loading -> {
                    binding.tvValidationMsg.text = "Loading squad... 🔄"
                    binding.tvValidationMsg.visibility = View.VISIBLE
                }

                is ApiResult.Success -> {
                    binding.tvValidationMsg.visibility = View.GONE
                    binding.btnSubmitTeam.isEnabled = true
                    allPlayers = result.data
                    applyFilter(currentFilter)

                    // Show data source info in a toast
                    val isRealData = result.data.any { it.playerId.length > 10 }
                    if (isRealData) {
                        Toast.makeText(
                            this,
                            "✅ Live squad data loaded!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                is ApiResult.Error -> {
                    binding.tvValidationMsg.visibility = View.GONE
                    binding.btnSubmitTeam.isEnabled = true
                    // Repository already fell back to dummy data
                    // allPlayers will be empty — trigger load from dummy directly
                    allPlayers = com.example.fantasycricketapp.utils.PlayerDummyData
                        .getPlayersForMatch(matchId)
                    applyFilter(currentFilter)
                }
            }
        }

        // Start loading
        teamViewModel.loadSquad(matchId)
    }

    private fun setupFilterTabs() {
        binding.tabAll.setOnClickListener  { applyFilter("ALL") }
        binding.tabBat.setOnClickListener  { applyFilter("BAT") }
        binding.tabBowl.setOnClickListener { applyFilter("BOWL") }
        binding.tabAR.setOnClickListener   { applyFilter("AR") }
        binding.tabWK.setOnClickListener   { applyFilter("WK") }
    }

    private fun applyFilter(filter: String) {
        currentFilter = filter
        updateTabUI(filter)

        // Use ViewModel's filter method if data is loaded
        val filtered = if (teamViewModel.getAllPlayers().isNotEmpty()) {
            teamViewModel.filterByRole(filter)
        } else {
            when (filter) {
                "BAT"  -> allPlayers.filter { it.role == "BAT" }
                "BOWL" -> allPlayers.filter { it.role == "BOWL" }
                "AR"   -> allPlayers.filter { it.role == "AR" }
                "WK"   -> allPlayers.filter { it.role == "WK" }
                else   -> allPlayers
            }
        }

        playerAdapter.submitList(filtered)
    }

    private fun updateTabUI(selected: String) {
        val allTabs = listOf(
            binding.tabAll,
            binding.tabBat,
            binding.tabBowl,
            binding.tabAR,
            binding.tabWK
        )

        allTabs.forEach { tab ->
            tab.setBackgroundResource(
                com.example.fantasycricketapp.R.drawable.bg_tab_unselected
            )
            tab.setTextColor(android.graphics.Color.parseColor("#8B949E"))
        }

        val activeTab: TextView = when (selected) {
            "BAT"  -> binding.tabBat
            "BOWL" -> binding.tabBowl
            "AR"   -> binding.tabAR
            "WK"   -> binding.tabWK
            else   -> binding.tabAll
        }

        activeTab.setBackgroundResource(
            com.example.fantasycricketapp.R.drawable.bg_tab_selected
        )
        activeTab.setTextColor(android.graphics.Color.WHITE)
    }

    private fun updateSelectionUI(selectedCount: Int) {
        binding.tvSelectedCount.text = "$selectedCount / 11 Players Selected"
        binding.progressSelection.progress = selectedCount

        val selectedPlayers = playerAdapter.getSelectedPlayers()
        val creditsUsed = selectedPlayers.sumOf { it.creditPoints }
        binding.tvCreditsUsed.text = "%.1f / 100".format(creditsUsed)

        binding.btnSubmitTeam.text = when {
            selectedCount < 11 -> "Submit Team ($selectedCount/11)"
            else               -> "✅ Submit Team (11/11)"
        }

        val buttonColor = if (selectedCount == 11) "#238636" else "#3D444D"
        binding.btnSubmitTeam.backgroundTintList =
            android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor(buttonColor)
            )

        binding.tvValidationMsg.visibility = View.GONE
    }

    private fun setupSubmitButton() {
        binding.btnSubmitTeam.setOnClickListener {
            handleSubmit()
        }
    }

    private fun handleSubmit() {
        val selectedPlayers = playerAdapter.getSelectedPlayers()
        val selectedCount = selectedPlayers.size

        if (selectedCount < 11) {
            val remaining = 11 - selectedCount
            showValidationError("Select $remaining more player${if (remaining > 1) "s" else ""}")
            return
        }

        val roleValidationError = validateRoleComposition(selectedPlayers)
        if (roleValidationError != null) {
            showValidationError(roleValidationError)
            return
        }

        // ✅ SUCCESS
        Toast.makeText(this, "Team Submitted Successfully!", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, ContestActivity::class.java)
        intent.putExtra("MATCH_ID", matchId)
        intent.putExtra("MATCH_NAME", matchName)
        startActivity(intent)

        finish()
    }

    private fun validateRoleComposition(players: List<Player>): String? {
        val batsmen = players.count { it.role == "BAT" }
        val bowlers = players.count { it.role == "BOWL" }
        val wicketKeepers = players.count { it.role == "WK" }

        return when {
            wicketKeepers < 1 -> "Include at least 1 WK"
            batsmen < 1       -> "Include at least 1 BAT"
            bowlers < 1       -> "Include at least 1 BOWL"
            else              -> null
        }
    }

    private fun showValidationError(message: String) {
        binding.tvValidationMsg.text = message
        binding.tvValidationMsg.visibility = View.VISIBLE
    }
}