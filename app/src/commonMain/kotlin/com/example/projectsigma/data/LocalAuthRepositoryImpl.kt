package com.example.projectsigma.data

import com.example.projectsigma.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocalAuthRepositoryImpl : AuthRepository {

    private data class StoredAccount(
        val user: User,
        val passwordHash: String
    )

    private val registeredAccounts = mutableMapOf<String, StoredAccount>()
    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        val testUser = User(
            id = "user_test_account_1",
            email = "test@example.com",
            displayName = "Test User",
            photoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
            eventsCount = 2
        )
        registeredAccounts["test@example.com"] = StoredAccount(
            user = testUser,
            passwordHash = "password123"
        )
    }

    override suspend fun loginWithEmail(email: String, pass: String): Result<User> {
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isBlank() || pass.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter your email and password."))
        }

        val account = registeredAccounts[trimmedEmail]
            ?: return Result.failure(IllegalArgumentException("No account found with this email. Please register or use test@example.com / password123."))

        if (account.passwordHash != pass) {
            return Result.failure(IllegalArgumentException("Incorrect password. Please try again."))
        }

        _currentUser.value = account.user
        return Result.success(account.user)
    }

    override suspend fun registerWithEmail(email: String, pass: String, name: String): Result<User> {
        val trimmedEmail = email.trim().lowercase()
        val trimmedName = name.trim()

        if (trimmedEmail.isBlank() || pass.length < 6 || trimmedName.isBlank()) {
            return Result.failure(IllegalArgumentException("Please complete all fields. Password must be at least 6 characters."))
        }

        if (registeredAccounts.containsKey(trimmedEmail)) {
            return Result.failure(IllegalArgumentException("An account with this email already exists. Please sign in."))
        }

        val newUser = User(
            id = "user_${trimmedEmail.hashCode()}",
            email = trimmedEmail,
            displayName = trimmedName,
            eventsCount = 0
        )

        registeredAccounts[trimmedEmail] = StoredAccount(
            user = newUser,
            passwordHash = pass
        )

        _currentUser.value = newUser
        return Result.success(newUser)
    }

    override suspend fun signInWithGoogle(): Result<User> {
        val googleUser = User(
            id = "user_google_account_99",
            email = "google.user@example.com",
            displayName = "Google User",
            photoUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
            eventsCount = 1
        )
        registeredAccounts[googleUser.email] = StoredAccount(googleUser, "google_oauth_pass")
        _currentUser.value = googleUser
        return Result.success(googleUser)
    }

    override fun logout() {
        _currentUser.value = null
    }

    override fun updateUserEventCount(count: Int) {
        _currentUser.value?.let { current ->
            val updated = current.copy(eventsCount = count)
            _currentUser.value = updated
            registeredAccounts[current.email]?.let { stored ->
                registeredAccounts[current.email] = stored.copy(user = updated)
            }
        }
    }
}
