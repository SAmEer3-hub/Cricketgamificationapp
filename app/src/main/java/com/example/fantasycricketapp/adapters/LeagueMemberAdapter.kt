package com.example.fantasycricketapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fantasycricketapp.R
import com.example.fantasycricketapp.data.model.LeagueMember
import com.example.fantasycricketapp.databinding.ItemLeagueMemberBinding
import com.example.fantasycricketapp.utils.FirebaseHelper

/**
 * LeagueMemberAdapter — Displays the leaderboard inside a league.
 *
 * Shows rank, name, team readiness, and score.
 * Highlights the current logged-in user with a "(You)" badge.
 * Shows gold/silver rank backgrounds for positions 1 and 2.
 *
 * @param isCompleted Whether the league is COMPLETED (shows actual scores vs dashes)
 */
class LeagueMemberAdapter(
    private val isCompleted: Boolean = false
) : ListAdapter<LeagueMember, LeagueMemberAdapter.MemberViewHolder>(MemberDiffCallback()) {

    private val currentUserId = FirebaseHelper.getCurrentUserId()

    inner class MemberViewHolder(
        private val binding: ItemLeagueMemberBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(member: LeagueMember, position: Int) {
            val context = binding.root.context
            val isMe = member.userId == currentUserId

            // ── Rank badge ──
            binding.tvMemberRank.text = "${position + 1}"
            val rankBackground = when (position) {
                0 -> R.drawable.bg_avatar_green   // 1st place — green
                else -> R.drawable.bg_avatar_gray // 2nd place — gray
            }
            binding.tvMemberRank.background = ContextCompat.getDrawable(context, rankBackground)

            // ── Name ──
            binding.tvMemberName.text = member.userName

            // ── "You" badge ──
            binding.tvYouBadge.visibility =
                if (isMe) android.view.View.VISIBLE else android.view.View.GONE

            // ── Team status ──
            if (member.hasTeam) {
                binding.tvMemberTeamStatus.text = "✅ Team submitted"
                binding.tvMemberTeamStatus.setTextColor(
                    android.graphics.Color.parseColor("#3FB950")
                )
            } else {
                binding.tvMemberTeamStatus.text = "⏳ Team not selected"
                binding.tvMemberTeamStatus.setTextColor(
                    android.graphics.Color.parseColor("#F0883E")
                )
            }

            // ── Score ──
            if (isCompleted) {
                binding.tvMemberScore.text = "${member.score}"
                // Highlight winner
                if (position == 0 && currentList.size > 1 &&
                    member.score >= currentList[1].score) {
                    binding.tvMemberScore.setTextColor(
                        android.graphics.Color.parseColor("#F0883E")
                    )
                }
            } else {
                binding.tvMemberScore.text = "—"
                binding.tvMemberScore.setTextColor(
                    android.graphics.Color.parseColor("#8B949E")
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val binding = ItemLeagueMemberBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MemberViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    class MemberDiffCallback : DiffUtil.ItemCallback<LeagueMember>() {
        override fun areItemsTheSame(oldItem: LeagueMember, newItem: LeagueMember) =
            oldItem.userId == newItem.userId
        override fun areContentsTheSame(oldItem: LeagueMember, newItem: LeagueMember) =
            oldItem == newItem
    }
}