package com.example.fantasycricketapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.fantasycricketapp.databinding.ActivityLoginBinding
import com.example.fantasycricketapp.ui.dashboard.MainActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {

        binding.btnLogin.setOnClickListener {
            handleLogin()
        }

        binding.tvGoToSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun handleLogin() {

        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Simple validation (no backend)
        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter email and password")
            return
        }

        showLoading(true)

        // Fake delay (simulate login)
        binding.root.postDelayed({
            showLoading(false)
            navigateToDashboard()
        }, 1000)
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
        binding.btnLogin.text = if (isLoading) "Logging in..." else "Login"
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }
}