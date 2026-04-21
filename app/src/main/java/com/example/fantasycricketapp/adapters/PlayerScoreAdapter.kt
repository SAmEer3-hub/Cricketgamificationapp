package com.example.fantasycricketapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fantasycricketapp.R
import com.example.fantasycricketapp.data.model.PlayerPoints
import com.example.fantasycricketapp.databinding.ItemPlayerScoreBinding

/**
 * PlayerScoreAdapter — Displays each player's fantasy points breakdown.
 */
class PlayerScoreAdapter :
    ListAdapter<PlayerPoints, PlayerScoreAdapter.ScoreViewHolder>(ScoreDiffCallback()) {

    inner class ScoreViewHolder(
        private val binding: ItemPlayerScoreBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(playerPoints: PlayerPoints, position: Int) {

            binding.tvRank.text = "#${position + 1}"

            applyRoleBadge(playerPoints.role)

            binding.tvScorePlayerName.text = playerPoints.playerName

            binding.tvScoreStats.text = buildStatsSummary(playerPoints)

            binding.tvScorePoints.text = "${playerPoints.totalPoints} pts"

            if (position == 0) {
                binding.tvRank.text = "🥇"
                binding.tvScorePoints.setTextColor(
                    android.graphics.Color.parseColor("#F0883E")
                )
            } else {
                binding.tvScorePoints.setTextColor(
                    android.graphics.Color.WHITE
                )
            }
        }

        private fun buildStatsSummary(p: PlayerPoints): String {
            val parts = mutableListOf<String>()

            if (p.runsScored > 0) {
                var batting = "${p.runsScored} runs"
                val extras = mutableListOf<String>()
                if (p.fours > 0)  extras.add("${p.fours}×4")
                if (p.sixes > 0)  extras.add("${p.sixes}×6")
                if (extras.isNotEmpty()) batting += " (${extras.joinToString(", ")})"
                parts.add(batting)
            } else if (p.isDuck) {
                parts.add("duck")
            }

            if (p.wicketsTaken > 0) parts.add("${p.wicketsTaken} wkt${if (p.wicketsTaken > 1) "s" else ""}")
            if (p.maidenOvers > 0)  parts.add("${p.maidenOvers} mdn")

            if (p.catches > 0)    parts.add("${p.catches} ct")
            if (p.stumpings > 0)  parts.add("${p.stumpings} st")
            if (p.runOuts > 0)    parts.add("${p.runOuts} ro")

            return if (parts.isEmpty()) "No contribution" else parts.joinToString(" | ")
        }

        private fun applyRoleBadge(role: String) {
            val context = binding.root.context
            val (label, drawable) = when (role) {
                "BOWL" -> "BOWL" to R.drawable.bg_role_bowl
                "AR"   -> "AR"   to R.drawable.bg_role_ar
                "WK"   -> "WK"   to R.drawable.bg_role_wk
                else   -> "BAT"  to R.drawable.bg_role_bat
            }
            binding.tvScoreRole.text = label
            binding.tvScoreRole.background = ContextCompat.getDrawable(context, drawable)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreViewHolder {
        val binding = ItemPlayerScoreBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ScoreViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScoreViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    class ScoreDiffCallback : DiffUtil.ItemCallback<PlayerPoints>() {
        override fun areItemsTheSame(oldItem: PlayerPoints, newItem: PlayerPoints) =
            oldItem.pointsId == newItem.pointsId

        override fun areContentsTheSame(oldItem: PlayerPoints, newItem: PlayerPoints) =
            oldItem == newItem
    }
}