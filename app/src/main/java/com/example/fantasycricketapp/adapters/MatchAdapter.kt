package com.example.fantasycricketapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fantasycricketapp.R
import com.example.fantasycricketapp.data.model.Match
import com.example.fantasycricketapp.databinding.ItemMatchBinding

class MatchAdapter(
    private val onPlayClick: (Match) -> Unit
) : ListAdapter<Match, MatchAdapter.MatchViewHolder>(MatchDiffCallback()) {

    inner class MatchViewHolder(
        private val binding: ItemMatchBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(match: Match) {

            binding.tvMatchType.text  = match.matchType
            binding.tvTeam1Flag.text  = match.team1Flag
            binding.tvTeam1Short.text = match.team1Short
            binding.tvTeam1Full.text  = match.team1
            binding.tvTeam2Flag.text  = match.team2Flag
            binding.tvTeam2Short.text = match.team2Short
            binding.tvTeam2Full.text  = match.team2
            binding.tvVenue.text      = match.venue
            binding.tvDate.text       = match.matchDate

            applyStatusBadge(match.status)

            when (match.status) {
                "COMPLETED" -> {
                    binding.btnPlayNow.text = "Match Ended"
                    binding.btnPlayNow.isEnabled = false
                    binding.btnPlayNow.alpha = 0.5f
                }
                "LIVE" -> {
                    binding.btnPlayNow.text = "🔴 Join Live Contest"
                    binding.btnPlayNow.isEnabled = true
                    binding.btnPlayNow.alpha = 1.0f
                }
                else -> {
                    binding.btnPlayNow.text = "Select Team & Play"
                    binding.btnPlayNow.isEnabled = true
                    binding.btnPlayNow.alpha = 1.0f
                }
            }

            binding.btnPlayNow.setOnClickListener {
                onPlayClick(match)
            }
        }

        private fun applyStatusBadge(status: String) {
            val context = binding.root.context

            when (status) {
                "LIVE" -> {
                    binding.tvStatus.text = "● LIVE"
                    binding.tvStatus.background = ContextCompat.getDrawable(
                        context, R.drawable.bg_status_live
                    )
                }
                "UPCOMING" -> {
                    binding.tvStatus.text = "UPCOMING"
                    binding.tvStatus.background = ContextCompat.getDrawable(
                        context, R.drawable.bg_status_upcoming
                    )
                }
                "COMPLETED" -> {
                    binding.tvStatus.text = "COMPLETED"
                    binding.tvStatus.background = ContextCompat.getDrawable(
                        context, R.drawable.bg_status_completed
                    )
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val binding = ItemMatchBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MatchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MatchDiffCallback : DiffUtil.ItemCallback<Match>() {

        override fun areItemsTheSame(oldItem: Match, newItem: Match): Boolean {
            return oldItem.matchId == newItem.matchId
        }

        override fun areContentsTheSame(oldItem: Match, newItem: Match): Boolean {
            return oldItem == newItem
        }
    }
}