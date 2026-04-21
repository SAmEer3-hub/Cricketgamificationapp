package com.example.fantasycricketapp.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val createdAt: Long = 0L,
    val totalPoints: Int = 0,
)

