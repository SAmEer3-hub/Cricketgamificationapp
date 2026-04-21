package com.example.fantasycricketapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fantasycricketapp.R
import com.example.fantasycricketapp.data.model.Player
import com.example.fantasycricketapp.databinding.ItemPlayerBinding

class PlayerAdapter(
    private val onSelectionChanged: (selectedCount: Int) -> Unit
) : ListAdapter<Player, PlayerAdapter.PlayerViewHolder>(PlayerDiffCallback()) {

    companion object {
        const val MAX_PLAYERS = 11
    }

    private val selectionState = mutableMapOf<String, Boolean>()

    inner class PlayerViewHolder(
        private val binding: ItemPlayerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(player: Player) {

            val isSelected = selectionState[player.playerId] ?: false
            val selectedCount = selectionState.values.count { it }

            binding.tvPlayerName.text  = player.name
            binding.tvTeam.text        = player.team
            binding.tvBattingStyle.text = player.battingStyle
            binding.tvCredits.text     = "${player.creditPoints} credits"

            applyRoleBadge(player.role)
            applySelectionState(isSelected)

            val isTeamFull = selectedCount >= MAX_PLAYERS
            val shouldDisable = isTeamFull && !isSelected
            binding.btnSelect.isEnabled = !shouldDisable
            binding.btnSelect.alpha = if (shouldDisable) 0.4f else 1.0f

            binding.btnSelect.setOnClickListener {
                toggleSelection(player)
            }

            binding.cardPlayer.setOnClickListener {
                if (binding.btnSelect.isEnabled) {
                    toggleSelection(player)
                }
            }
        }

        private fun toggleSelection(player: Player) {
            val currentlySelected = selectionState[player.playerId] ?: false
            val newState = !currentlySelected
            selectionState[player.playerId] = newState

            val newCount = selectionState.values.count { it }
            onSelectionChanged(newCount)

            notifyItemChanged(adapterPosition)
            notifyDataSetChanged()
        }

        private fun applyRoleBadge(role: String) {
            val context = binding.root.context
            when (role) {
                "BAT" -> {
                    binding.tvRole.text = "BAT"
                    binding.tvRole.background = ContextCompat.getDrawable(
                        context, R.drawable.bg_role_bat
                    )
                }
                "BOWL" -> {
                    binding.tvRole.text = "BOWL"
                    binding.tvRole.background = ContextCompat.getDrawable(
                        context, R.drawable.bg_role_bowl
                    )
                }
                "AR" -> {
                    binding.tvRole.text = "AR"
                    binding.tvRole.background = ContextCompat.getDrawable(
                        context, R.drawable.bg_role_ar
                    )
                }
                "WK" -> {
                    binding.tvRole.text = "WK"
                    binding.tvRole.background = ContextCompat.getDrawable(
                        context, R.drawable.bg_role_wk
                    )
                }
            }
        }

        private fun applySelectionState(isSelected: Boolean) {
            val context = binding.root.context
            if (isSelected) {
                binding.cardPlayer.background = ContextCompat.getDrawable(
                    context, R.drawable.bg_player_selected
                )
                binding.btnSelect.text = "Remove"
                binding.btnSelect.setBackgroundColor(
                    ContextCompat.getColor(context, android.R.color.holo_red_dark)
                )
            } else {
                binding.cardPlayer.setCardBackgroundColor(
                    android.graphics.Color.parseColor("#161B22")
                )
                binding.cardPlayer.background = null
                binding.btnSelect.text = "Select"
                binding.btnSelect.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#1F6FEB")
                    )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val binding = ItemPlayerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlayerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun getSelectedPlayers(): List<Player> {
        return currentList.filter { player ->
            selectionState[player.playerId] == true
        }
    }

    fun getSelectedCount(): Int {
        return selectionState.values.count { it }
    }

    fun clearSelections() {
        selectionState.clear()
        notifyDataSetChanged()
    }

    class PlayerDiffCallback : DiffUtil.ItemCallback<Player>() {
        override fun areItemsTheSame(oldItem: Player, newItem: Player): Boolean {
            return oldItem.playerId == newItem.playerId
        }
        override fun areContentsTheSame(oldItem: Player, newItem: Player): Boolean {
            return oldItem == newItem
        }
    }
}