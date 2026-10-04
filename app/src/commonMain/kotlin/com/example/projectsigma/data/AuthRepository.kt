package com.example.projectsigma.data

import com.example.projectsigma.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepository {

    private val _currentUser = MutableStateFlow<User?>(
        User(
            id = "user_demo_123",
            email = "alex.smith@example.com",
            displayName = "Alex Smith",
            photoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
            eventsCount = 3
        )
    )
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    suspend fun loginWithEmail(email: String, pass: String): Result<User> {
        if (email.isBlank() || pass.length < 6) {
            return Result.failure(IllegalArgumentException("Please enter a valid email and password (min 6 characters)."))
        }
        val user = User(
            id = "user_${email.hashCode()}",
            email = email,
            displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() },
            eventsCount = 1
        )
        _currentUser.value = user
        return Result.success(user)
    }

    suspend fun registerWithEmail(email: String, pass: String, name: String): Result<User> {
        if (email.isBlank() || pass.length < 6 || name.isBlank()) {
            return Result.failure(IllegalArgumentException("Please complete all fields with a valid password."))
        }
        val user = User(
            id = "user_${email.hashCode()}",
            email = email,
            displayName = name,
            eventsCount = 0
        )
        _currentUser.value = user
        return Result.success(user)
    }

    suspend fun signInWithGoogle(): Result<User> {
        val googleUser = User(
            id = "user_google_99",
            email = "alex.google@gmail.com",
            displayName = "Alex (Google Account)",
            photoUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
            eventsCount = 2
        )
        _currentUser.value = googleUser
        return Result.success(googleUser)
    }

    fun logout() {
        _currentUser.value = null
    }

    fun updateUserEventCount(count: Int) {
        _currentUser.value?.let { current ->
            _currentUser.value = current.copy(eventsCount = count)
        }
    }
}
