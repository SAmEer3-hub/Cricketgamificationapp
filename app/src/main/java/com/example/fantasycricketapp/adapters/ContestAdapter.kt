package com.example.fantasycricketapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fantasycricketapp.R
import com.example.fantasycricketapp.data.model.Contest
import com.example.fantasycricketapp.databinding.ItemContestBinding
import com.example.fantasycricketapp.utils.FirebaseHelper

class ContestAdapter(
    private val onViewClick: (Contest) -> Unit
) : ListAdapter<Contest, ContestAdapter.ContestViewHolder>(ContestDiffCallback()) {

    private val currentUserId = FirebaseHelper.getCurrentUserId()

    inner class ContestViewHolder(
        private val binding: ItemContestBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(contest: Contest) {

            binding.tvContestMatchName.text = contest.matchName

            val shortId = if (contest.contestId.length > 8)
                "#${contest.contestId.takeLast(8).uppercase()}"
            else "#${contest.contestId.uppercase()}"
            binding.tvContestId.text = shortId

            applyStatusBadge(contest.status)

            val isUser1 = contest.user1Id == currentUserId

            val myName       = if (isUser1) contest.user1Name else contest.user2Name
            val opponentName = if (isUser1) contest.user2Name else contest.user1Name
            val myScore      = if (isUser1) contest.user1Score else contest.user2Score
            val opponentScore= if (isUser1) contest.user2Score else contest.user1Score

            binding.tvUser1Name.text   = "You ($myName)"
            binding.tvUser1Avatar.text = myName.firstOrNull()?.uppercaseChar()?.toString() ?: "Y"
            binding.tvUser1Score.text  = if (contest.status == "COMPLETED") "$myScore pts" else "- pts"

            if (contest.user2Id.isEmpty()) {
                binding.tvUser2Name.text   = "Waiting for opponent..."
                binding.tvUser2Avatar.text = "?"
                binding.tvUser2Score.text  = "- pts"
                binding.tvUser2Avatar.background = ContextCompat.getDrawable(
                    binding.root.context, R.drawable.bg_avatar_gray
                )
            } else {
                binding.tvUser2Name.text   = opponentName
                binding.tvUser2Avatar.text = opponentName.firstOrNull()?.uppercaseChar()?.toString() ?: "P"
                binding.tvUser2Score.text  = if (contest.status == "COMPLETED") "$opponentScore pts" else "- pts"
                binding.tvUser2Avatar.background = ContextCompat.getDrawable(
                    binding.root.context, R.drawable.bg_avatar_green
                )
            }

            binding.btnViewContest.setOnClickListener {
                onViewClick(contest)
            }
        }

        private fun applyStatusBadge(status: String) {
            val context = binding.root.context
            when (status) {
                "WAITING" -> {
                    binding.tvContestStatus.text = "⏳ WAITING"
                    binding.tvContestStatus.background = ContextCompat.getDrawable(
                        context, R.drawable.bg_status_upcoming
                    )
                }
                "ACTIVE" -> {
                    binding.tvContestStatus.text = "● ACTIVE"
                    binding.tvContestStatus.background = ContextCompat.getDrawable(
                        context, R.drawable.bg_status_live
                    )
                }
                "COMPLETED" -> {
                    binding.tvContestStatus.text = "✓ DONE"
                    binding.tvContestStatus.background = ContextCompat.getDrawable(
                        context, R.drawable.bg_status_completed
                    )
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContestViewHolder {
        val binding = ItemContestBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ContestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContestViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ContestDiffCallback : DiffUtil.ItemCallback<Contest>() {
        override fun areItemsTheSame(oldItem: Contest, newItem: Contest) =
            oldItem.contestId == newItem.contestId

        override fun areContentsTheSame(oldItem: Contest, newItem: Contest) =
            oldItem == newItem
    }
}