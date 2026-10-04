package com.example.projectsigma.data

import com.example.projectsigma.model.User
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<User?>
    suspend fun loginWithEmail(email: String, pass: String): Result<User>
    suspend fun registerWithEmail(email: String, pass: String, name: String): Result<User>
    suspend fun signInWithGoogle(): Result<User>
    fun logout()
    fun updateUserEventCount(count: Int)
}
