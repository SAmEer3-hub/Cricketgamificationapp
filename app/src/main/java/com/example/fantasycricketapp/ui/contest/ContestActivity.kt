package com.example.fantasycricketapp.ui.contest

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fantasycricketapp.databinding.ActivityContestBinding
import com.example.fantasycricketapp.ui.result.ResultActivity

class ContestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContestBinding

    private var matchId: String = ""
    private var matchName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityContestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        matchId = intent.getStringExtra("MATCH_ID") ?: ""
        matchName = intent.getStringExtra("MATCH_NAME") ?: "Match"

        // ✅ Now works perfectly
        binding.tvContestMatchName.text = matchName
        binding.tvSelectedMatch.text = matchName

        binding.btnJoinContest.setOnClickListener {
            Toast.makeText(this, "Joined Contest!", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, ResultActivity::class.java)
            intent.putExtra("MATCH_ID", matchId)
            intent.putExtra("MATCH_NAME", matchName)
            startActivity(intent)
        }
    }
}