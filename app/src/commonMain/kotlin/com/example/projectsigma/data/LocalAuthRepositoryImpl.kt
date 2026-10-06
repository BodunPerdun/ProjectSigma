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

    private val nearbySampleUsers = listOf(
        Pair(
            User(
                id = "usr_near_1",
                email = "elena@example.com",
                displayName = "Elena Rostova",
                photoUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
                eventsCount = 4,
                isLocationVisible = true,
                isSocialsPublic = true,
                instagramHandle = "elena_rostova",
                telegramHandle = "elena_r",
                bio = "Software engineer & jazz enthusiast in London 🎷☕"
            ),
            "120m away"
        ),
        Pair(
            User(
                id = "usr_near_2",
                email = "mark@example.com",
                displayName = "Mark Vance",
                photoUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
                eventsCount = 2,
                isLocationVisible = true,
                isSocialsPublic = true,
                instagramHandle = "mark_vance",
                telegramHandle = "markv_dev",
                bio = "Co-founder at TechVentures. Always open to new startup ideas! 🚀"
            ),
            "340m away"
        ),
        Pair(
            User(
                id = "usr_near_3",
                email = "sophia@example.com",
                displayName = "Sophia Miller",
                photoUrl = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=150",
                eventsCount = 5,
                isLocationVisible = true,
                isSocialsPublic = false,
                instagramHandle = "sophia_m",
                telegramHandle = "sophia_m",
                bio = "Art gallery curator, digital sculpture lover, and foodie. 🎨🍕"
            ),
            "750m away"
        ),
        Pair(
            User(
                id = "usr_near_4",
                email = "lucas@example.com",
                displayName = "Lucas Wright",
                photoUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150",
                eventsCount = 1,
                isLocationVisible = true,
                isSocialsPublic = true,
                telegramHandle = "lucas_wright",
                bio = "Sports & fitness coach. Running, cycling, and outdoor events! 🏃‍♂️🚴‍♂️"
            ),
            "1.2km away"
        )
    )

    init {
        val testUser = User(
            id = "user_test_account_1",
            email = "test@example.com",
            displayName = "Test User",
            photoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
            eventsCount = 2,
            isLocationVisible = false,
            isSocialsPublic = true,
            friends = listOf("usr_near_1"),
            instagramHandle = "test_user_app",
            telegramHandle = "test_user_tg",
            bio = "Love jazz music, tech meetups, and outdoor sports in London! 🎷☕"
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
            eventsCount = 0,
            isLocationVisible = false,
            isSocialsPublic = true
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
            eventsCount = 1,
            isLocationVisible = false,
            isSocialsPublic = true
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

    override fun updateLocationVisibility(isVisible: Boolean) {
        _currentUser.value?.let { current ->
            val updated = current.copy(isLocationVisible = isVisible)
            _currentUser.value = updated
            registeredAccounts[current.email]?.let { stored ->
                registeredAccounts[current.email] = stored.copy(user = updated)
            }
        }
    }

    override fun updateSocialsPublicity(isPublic: Boolean) {
        _currentUser.value?.let { current ->
            val updated = current.copy(isSocialsPublic = isPublic)
            _currentUser.value = updated
            registeredAccounts[current.email]?.let { stored ->
                registeredAccounts[current.email] = stored.copy(user = updated)
            }
        }
    }

    override fun updateProfilePhoto(photoUrl: String?) {
        _currentUser.value?.let { current ->
            val updated = current.copy(
                photoUrl = photoUrl,
                avatarSyncStatus = "PENDING_PUSH"
            )
            _currentUser.value = updated
            registeredAccounts[current.email]?.let { stored ->
                registeredAccounts[current.email] = stored.copy(user = updated)
            }
        }
    }

    override fun updateSocialHandles(instagram: String?, telegram: String?) {
        _currentUser.value?.let { current ->
            val updated = current.copy(
                instagramHandle = instagram?.trim()?.removePrefix("@"),
                telegramHandle = telegram?.trim()?.removePrefix("@")
            )
            _currentUser.value = updated
            registeredAccounts[current.email]?.let { stored ->
                registeredAccounts[current.email] = stored.copy(user = updated)
            }
        }
    }

    override fun updateUserBio(bio: String?) {
        _currentUser.value?.let { current ->
            val updated = current.copy(bio = bio?.trim())
            _currentUser.value = updated
            registeredAccounts[current.email]?.let { stored ->
                registeredAccounts[current.email] = stored.copy(user = updated)
            }
        }
    }

    override fun addFriend(friendId: String) {
        _currentUser.value?.let { current ->
            if (!current.friends.contains(friendId)) {
                val updatedFriends = current.friends + friendId
                val updated = current.copy(friends = updatedFriends)
                _currentUser.value = updated
                registeredAccounts[current.email]?.let { stored ->
                    registeredAccounts[current.email] = stored.copy(user = updated)
                }
            }
        }
    }

    override fun removeFriend(friendId: String) {
        _currentUser.value?.let { current ->
            if (current.friends.contains(friendId)) {
                val updatedFriends = current.friends - friendId
                val updated = current.copy(friends = updatedFriends)
                _currentUser.value = updated
                registeredAccounts[current.email]?.let { stored ->
                    registeredAccounts[current.email] = stored.copy(user = updated)
                }
            }
        }
    }

    override fun getDiscoverableNearbyUsers(): List<Pair<User, String>> {
        return nearbySampleUsers
    }
}
