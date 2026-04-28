package com.example.seabattle.data

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) {

    fun currentUserId(): String? = auth.currentUser?.uid

    fun currentUserEmail(): String? = auth.currentUser?.email

    fun signOut() {
        auth.signOut()
    }

    suspend fun signIn(email: String, password: String): String {
        return auth.signInWithEmailAndPassword(email.trim(), password)
            .await()
            .user
            ?.uid
            ?: error("Email sign-in returned no user")
    }

    suspend fun register(email: String, password: String): String {
        return auth.createUserWithEmailAndPassword(email.trim(), password)
            .await()
            .user
            ?.uid
            ?: error("Registration returned no user")
    }
}
