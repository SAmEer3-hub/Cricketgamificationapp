package com.example.fantasycricketapp.data.repository

import com.google.firebase.auth.FirebaseUser
import com.example.fantasycricketapp.data.model.User
import com.example.fantasycricketapp.utils.FirebaseHelper

class AuthRepository {

    private val auth = FirebaseHelper.auth
    private val firestore = FirebaseHelper.firestore

    fun signUp(
        name: String,
        email: String,
        password: String,
        onSuccess: (FirebaseUser) -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->

                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    saveUserToFirestore(
                        uid = firebaseUser.uid,
                        name = name,
                        email = email,
                        onSuccess = { onSuccess(firebaseUser) },
                        onFailure = onFailure
                    )
                } else {
                    onFailure("Signup failed: User is null")
                }
            }
            .addOnFailureListener {
                onFailure(it.message ?: "Signup failed. Please try again.")
            }
    }

    private fun saveUserToFirestore(
        uid: String,
        name: String,
        email: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val user = User(
            uid = uid,
            name = name,
            email = email,
            totalPoints = 0,
            createdAt = System.currentTimeMillis()
        )

        firestore
            .collection(FirebaseHelper.COLLECTION_USERS)
            .document(uid)
            .set(user)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                onFailure(it.message ?: "Failed to save user profile.")
            }
    }

    fun login(
        email: String,
        password: String,
        onSuccess: (FirebaseUser) -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                authResult.user?.let { onSuccess(it) }
                    ?: onFailure("Login failed: User not found.")
            }
            .addOnFailureListener {
                onFailure(it.message ?: "Login failed. Please try again.")
            }
    }

    fun logout() {
        auth.signOut()
    }

    fun isUserLoggedIn(): Boolean {
        return true //  FORCE LOGIN

    }
}