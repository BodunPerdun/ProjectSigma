package com.example.projectsigma.data.local

import android.content.Context
import android.util.Log
import com.example.projectsigma.data.AuthRepository
import com.example.projectsigma.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class PersistentAuthRepositoryImpl(private val context: Context) : AuthRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val accountFile: File by lazy {
        File(context.filesDir, "persistent_user_account.json")
    }

    private val accountsMapFile: File by lazy {
        File(context.filesDir, "persistent_user_accounts_map.json")
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val testAccount = User(
        id = "user_test_account_1",
        email = "test@example.com",
        displayName = "Test User",
        photoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
        eventsCount = 2,
        isLocationVisible = true,
        isSocialsPublic = true,
        friends = listOf("usr_near_1"),
        instagramHandle = "test_user_app",
        telegramHandle = "test_user_tg",
        bio = "Love jazz music, tech meetups, and outdoor sports in London! 🎷☕"
    )

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

    private var registeredAccountsMap = mutableMapOf<String, User>()

    init {
        loadAccountsMapFromDisk()
        loadUserFromDisk()
    }

    private fun loadAccountsMapFromDisk() {
        try {
            if (accountsMapFile.exists()) {
                val content = accountsMapFile.readText()
                if (content.isNotBlank()) {
                    registeredAccountsMap = json.decodeFromString<MutableMap<String, User>>(content)
                }
            }
        } catch (e: Exception) {
            Log.e("PersistentAuthRepo", "Error loading accounts map: ${e.message}")
        }
        if (!registeredAccountsMap.containsKey("test@example.com")) {
            registeredAccountsMap["test@example.com"] = testAccount
        }
    }

    private fun saveAccountsMapToDisk() {
        scope.launch {
            try {
                val content = json.encodeToString(registeredAccountsMap)
                accountsMapFile.writeText(content)
            } catch (e: Exception) {
                Log.e("PersistentAuthRepo", "Error saving accounts map: ${e.message}")
            }
        }
    }

    private fun loadUserFromDisk() {
        try {
            if (accountFile.exists()) {
                val content = accountFile.readText()
                if (content.isNotBlank()) {
                    val loadedUser = json.decodeFromString<User>(content)
                    _currentUser.value = loadedUser
                    Log.d("PersistentAuthRepo", "Loaded user profile ${loadedUser.displayName} with avatar ${loadedUser.photoUrl}")
                    return
                }
            }
        } catch (e: Exception) {
            Log.e("PersistentAuthRepo", "Error loading user account from disk: ${e.message}")
        }

        val defaultUser = registeredAccountsMap["test@example.com"] ?: testAccount
        _currentUser.value = defaultUser
        saveUserToDisk(defaultUser)
    }

    private fun saveUserToDisk(user: User?) {
        scope.launch {
            try {
                if (user == null) {
                    if (accountFile.exists()) accountFile.delete()
                } else {
                    val content = json.encodeToString(user)
                    accountFile.writeText(content)
                    registeredAccountsMap[user.email] = user
                    saveAccountsMapToDisk()
                    Log.d("PersistentAuthRepo", "Saved user profile ${user.displayName} to disk")
                }
            } catch (e: Exception) {
                Log.e("PersistentAuthRepo", "Error saving user to disk: ${e.message}")
            }
        }
    }

    override suspend fun loginWithEmail(email: String, pass: String): Result<User> {
        val trimmedEmail = email.trim().lowercase()
        val existing = registeredAccountsMap[trimmedEmail]
        val user = existing ?: testAccount.copy(
            id = "user_${trimmedEmail.hashCode()}",
            email = trimmedEmail,
            displayName = trimmedEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
        )
        _currentUser.value = user
        saveUserToDisk(user)
        return Result.success(user)
    }

    override suspend fun registerWithEmail(email: String, pass: String, name: String): Result<User> {
        val trimmedEmail = email.trim().lowercase()
        val user = User(
            id = "user_${trimmedEmail.hashCode()}",
            email = trimmedEmail,
            displayName = name,
            eventsCount = 0,
            isLocationVisible = true,
            isSocialsPublic = true
        )
        _currentUser.value = user
        saveUserToDisk(user)
        return Result.success(user)
    }

    override suspend fun signInWithGoogle(): Result<User> {
        val defaultUser = registeredAccountsMap["test@example.com"] ?: testAccount
        _currentUser.value = defaultUser
        saveUserToDisk(defaultUser)
        return Result.success(defaultUser)
    }

    override fun logout() {
        _currentUser.value = null
        saveUserToDisk(null)
    }

    override fun updateUserEventCount(count: Int) {
        _currentUser.value?.let { current ->
            val updated = current.copy(eventsCount = count)
            _currentUser.value = updated
            saveUserToDisk(updated)
        }
    }

    override fun updateLocationVisibility(isVisible: Boolean) {
        _currentUser.value?.let { current ->
            val updated = current.copy(isLocationVisible = isVisible)
            _currentUser.value = updated
            saveUserToDisk(updated)
        }
    }

    override fun updateSocialsPublicity(isPublic: Boolean) {
        _currentUser.value?.let { current ->
            val updated = current.copy(isSocialsPublic = isPublic)
            _currentUser.value = updated
            saveUserToDisk(updated)
        }
    }

    override fun updateProfilePhoto(photoUrl: String?) {
        _currentUser.value?.let { current ->
            val updated = current.copy(photoUrl = photoUrl, avatarSyncStatus = "PENDING_PUSH")
            _currentUser.value = updated
            saveUserToDisk(updated)
        }
    }

    override fun updateSocialHandles(instagram: String?, telegram: String?) {
        _currentUser.value?.let { current ->
            val updated = current.copy(
                instagramHandle = instagram?.trim()?.removePrefix("@"),
                telegramHandle = telegram?.trim()?.removePrefix("@")
            )
            _currentUser.value = updated
            saveUserToDisk(updated)
        }
    }

    override fun updateUserBio(bio: String?) {
        _currentUser.value?.let { current ->
            val updated = current.copy(bio = bio?.trim())
            _currentUser.value = updated
            saveUserToDisk(updated)
        }
    }

    override fun addFriend(friendId: String) {
        _currentUser.value?.let { current ->
            if (!current.friends.contains(friendId)) {
                val updated = current.copy(friends = current.friends + friendId)
                _currentUser.value = updated
                saveUserToDisk(updated)
            }
        }
    }

    override fun removeFriend(friendId: String) {
        _currentUser.value?.let { current ->
            if (current.friends.contains(friendId)) {
                val updated = current.copy(friends = current.friends - friendId)
                _currentUser.value = updated
                saveUserToDisk(updated)
            }
        }
    }

    override fun getDiscoverableNearbyUsers(): List<Pair<User, String>> {
        return nearbySampleUsers
    }
}
