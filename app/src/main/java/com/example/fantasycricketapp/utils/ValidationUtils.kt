
package com.example.fantasycricketapp.utils

object ValidationUtils {

    fun validateName(name: String): String? {
        return if (name.isEmpty()) "Name cannot be empty" else null
    }

    fun validateEmail(email: String): String? {
        return if (email.isEmpty()) {
            "Email cannot be empty"
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            "Invalid email format"
        } else null
    }

    fun validatePassword(password: String): String? {
        return if (password.length < 6) {
            "Password must be at least 6 characters"
        } else null
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        return if (password != confirmPassword) {
            "Passwords do not match"
        } else null
    }
}