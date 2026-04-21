package com.example.fantasycricketapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.fantasycricketapp.data.repository.AuthRepository
import com.example.fantasycricketapp.databinding.ActivitySignupBinding
import com.example.fantasycricketapp.ui.dashboard.MainActivity
import com.example.fantasycricketapp.utils.ValidationUtils

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {

        binding.btnSignup.setOnClickListener {
            handleSignup()
        }

        binding.tvGoToLogin.setOnClickListener {
            finish()
        }
    }

    private fun handleSignup() {

        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        val nameError = ValidationUtils.validateName(name)
        val emailError = ValidationUtils.validateEmail(email)
        val passwordError = ValidationUtils.validatePassword(password)
        val confirmError = ValidationUtils.validateConfirmPassword(password, confirmPassword)

        binding.tilName.error = nameError
        binding.tilEmail.error = emailError
        binding.tilPassword.error = passwordError
        binding.tilConfirmPassword.error = confirmError

        if (nameError != null || emailError != null ||
            passwordError != null || confirmError != null) return

        showLoading(true)
        hideMessages()

        authRepository.signUp(
            name = name,
            email = email,
            password = password,
            onSuccess = {
                showLoading(false)
                showSuccess("Account created! Welcome, $name 🎉")

                binding.root.postDelayed({
                    navigateToDashboard()
                }, 1200)
            },
            onFailure = { errorMessage ->
                showLoading(false)
                showError(errorMessage)
            }
        )
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSignup.isEnabled = !isLoading
        binding.btnSignup.text = if (isLoading) "Creating account..." else "Create Account"
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
        binding.tvSuccess.visibility = View.GONE
    }

    private fun showSuccess(message: String) {
        binding.tvSuccess.text = message
        binding.tvSuccess.visibility = View.VISIBLE
        binding.tvError.visibility = View.GONE
    }

    private fun hideMessages() {
        binding.tvError.visibility = View.GONE
        binding.tvSuccess.visibility = View.GONE
    }
}