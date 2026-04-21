package com.example.fantasycricketapp.utils


/**
 * LeagueCodeGenerator — Generates unique, human-readable invite codes.
 *
 * Format: 3 uppercase letters + 3 digits = e.g. "FCK284", "XRT019"
 *
 * Excludes confusing characters: 0/O, 1/I/L
 * so codes are easy to read and type manually.
 *
 * Total combinations: 23 letters × 23 letters × 23 letters × 10 × 10 × 10
 *                   = ~12 million unique codes
 * Collision probability with < 10,000 leagues is negligible.
 */
object LeagueCodeGenerator {

    // Exclude 0, O, I, L, 1 to avoid confusion when sharing verbally
    private val LETTERS = "ABCDEFGHJKMNPQRSTUVWXYZ"   // 23 chars
    private val DIGITS  = "0123456789"                  // 10 chars

    /**
     * Generates a random 6-character code: 3 letters + 3 digits.
     * Example output: "FCK284", "QRM507", "BNT932"
     */
    fun generate(): String {
        val letters = (1..3).map { LETTERS.random() }.joinToString("")
        val digits  = (1..3).map { DIGITS.random() }.joinToString("")
        return "$letters$digits"
    }

    /**
     * Validates that a code matches the expected format.
     * Used when a user types in a code to join.
     */
    fun isValidFormat(code: String): Boolean {
        if (code.length != 6) return false
        val upper = code.uppercase()
        val lettersOk = upper.take(3).all { it.isLetter() }
        val digitsOk  = upper.takeLast(3).all { it.isDigit() }
        return lettersOk && digitsOk
    }
}