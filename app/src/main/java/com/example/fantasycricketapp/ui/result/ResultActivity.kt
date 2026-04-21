package com.example.fantasycricketapp.ui.result

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fantasycricketapp.databinding.ActivityResultBinding
import com.example.fantasycricketapp.ui.dashboard.MainActivity

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    private var matchName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        matchName = intent.getStringExtra("MATCH_NAME") ?: "Match"

        setupUI()
        setupButtons()
    }

    private fun setupUI() {

        binding.tvResultMatchName.text = matchName

        // ✅ DEMO RESULT
        val myScore = (80..120).random()
        val opponentScore = (70..110).random()

        binding.tvResultUser1Name.text = "You"
        binding.tvResultUser2Name.text = "Opponent"

        binding.tvResultUser1Score.text = "$myScore pts"
        binding.tvResultUser2Score.text = "$opponentScore pts"

        if (myScore > opponentScore) {
            binding.tvWinnerLabel.text = "🎉 YOU WIN!"
            binding.tvWinnerName.text = "You"
            binding.tvWinnerScore.text = "$myScore pts"
        } else {
            binding.tvWinnerLabel.text = "😔 YOU LOST"
            binding.tvWinnerName.text = "Opponent"
            binding.tvWinnerScore.text = "$opponentScore pts"
        }

        binding.layoutResult.visibility = android.view.View.VISIBLE
    }

    private fun setupButtons() {

        binding.btnGoToDashboard.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnPlayAgain.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}