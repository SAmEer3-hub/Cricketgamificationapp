package com.example.fantasycricketapp.adapters



import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fantasycricketapp.data.model.LeagueMessage
import com.example.fantasycricketapp.databinding.ItemChatMessageBinding
import com.example.fantasycricketapp.utils.FirebaseHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ChatAdapter — Displays real-time chat messages inside a league.
 *
 * Sent messages (by current user):
 *   - Aligned to the RIGHT
 *   - Blue bubble background
 *   - Sender name hidden (it's always "You")
 *
 * Received messages (by opponent):
 *   - Aligned to the LEFT
 *   - Dark gray bubble background
 *   - Sender name shown above bubble
 */
class ChatAdapter : ListAdapter<LeagueMessage, ChatAdapter.ChatViewHolder>(ChatDiffCallback()) {

    private val currentUserId = FirebaseHelper.getCurrentUserId()
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    inner class ChatViewHolder(
        private val binding: ItemChatMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: LeagueMessage) {
            val isSent = message.userId == currentUserId

            // ── Message text ──
            binding.tvChatText.text = message.text

            // ── Timestamp ──
            binding.tvChatTime.text = timeFormat.format(Date(message.timestamp))

            if (isSent) {
                // ── SENT: right-aligned blue bubble ──
                binding.tvChatSenderName.visibility = View.GONE
                binding.spacerLeft.layoutParams = LinearWeight(1f)
                binding.spacerRight.layoutParams = LinearWeight(0f)
                binding.layoutBubble.setBackgroundResource(
                    com.example.fantasycricketapp.R.drawable.bg_chat_sent
                )
                // Right-align time
                binding.tvChatTime.layoutParams =
                    (binding.tvChatTime.layoutParams as ViewGroup.MarginLayoutParams).apply {
                        // gravity end is set in XML already
                    }
            } else {
                // ── RECEIVED: left-aligned gray bubble ──
                binding.tvChatSenderName.visibility = View.VISIBLE
                binding.tvChatSenderName.text = message.userName
                binding.spacerLeft.layoutParams = LinearWeight(0f)
                binding.spacerRight.layoutParams = LinearWeight(1f)
                binding.layoutBubble.setBackgroundResource(
                    com.example.fantasycricketapp.R.drawable.bg_chat_received
                )
            }
        }

        /**
         * Helper to set layout_weight programmatically.
         */
        private fun LinearWeight(weight: Float): android.widget.LinearLayout.LayoutParams {
            return android.widget.LinearLayout.LayoutParams(
                0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, weight
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ChatDiffCallback : DiffUtil.ItemCallback<LeagueMessage>() {
        override fun areItemsTheSame(oldItem: LeagueMessage, newItem: LeagueMessage) =
            oldItem.messageId == newItem.messageId
        override fun areContentsTheSame(oldItem: LeagueMessage, newItem: LeagueMessage) =
            oldItem == newItem
    }
}