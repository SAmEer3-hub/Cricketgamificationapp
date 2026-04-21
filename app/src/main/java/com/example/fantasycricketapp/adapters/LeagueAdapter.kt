package com.example.fantasycricketapp.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fantasycricketapp.R
import com.example.fantasycricketapp.data.model.League
import com.example.fantasycricketapp.databinding.ItemLeagueBinding

/**
 * LeagueAdapter — Displays private leagues in a RecyclerView.
 *
 * Each card shows:
 * - League name + status badge
 * - Match name + members count
 * - Invite code (tappable to copy)
 * - Share button (opens Android share sheet)
 * - Open button (navigates to LeagueDetailActivity)
 *
 * @param onOpenClick  Lambda called when "View League" is tapped
 * @param onShareClick Lambda called when "Share" is tapped — receives the invite code
 */
class LeagueAdapter(
    private val onOpenClick: (League) -> Unit,
    private val onShareClick: (League) -> Unit
) : ListAdapter<League, LeagueAdapter.LeagueViewHolder>(LeagueDiffCallback()) {

    inner class LeagueViewHolder(
        private val binding: ItemLeagueBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(league: League) {
            val context = binding.root.context

            // ── Basic info ──
            binding.tvLeagueName.text        = league.leagueName
            binding.tvLeagueMatchName.text   = "🏏 ${league.matchName}"
            binding.tvLeagueCode.text        = league.leagueCode
            binding.tvLeagueMembersCount.text =
                "${league.members.size} / ${league.maxMembers} members"

            // ── Status badge ──
            applyStatusBadge(league.status)

            // ── Share button ──
            binding.btnShareLeague.setOnClickListener {
                onShareClick(league)
            }

            // ── Open league ──
            binding.btnOpenLeague.setOnClickListener {
                onOpenClick(league)
            }
        }

        private fun applyStatusBadge(status: String) {
            val context = binding.root.context
            when (status) {
                "OPEN" -> {
                    binding.tvLeagueStatus.text = "⏳ OPEN"
                    binding.tvLeagueStatus.background = ContextCompat.getDrawable(
                        context, R.drawable.bg_status_upcoming
                    )
                }
                "ACTIVE" -> {
                    binding.tvLeagueStatus.text = "● ACTIVE"
                    binding.tvLeagueStatus.background = ContextCompat.getDrawable(
                        context, R.drawable.bg_status_live
                    )
                }
                "COMPLETED" -> {
                    binding.tvLeagueStatus.text = "✓ DONE"
                    binding.tvLeagueStatus.background = ContextCompat.getDrawable(
                        context, R.drawable.bg_status_completed
                    )
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeagueViewHolder {
        val binding = ItemLeagueBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LeagueViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LeagueViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class LeagueDiffCallback : DiffUtil.ItemCallback<League>() {
        override fun areItemsTheSame(oldItem: League, newItem: League) =
            oldItem.leagueCode == newItem.leagueCode
        override fun areContentsTheSame(oldItem: League, newItem: League) =
            oldItem == newItem
    }
}