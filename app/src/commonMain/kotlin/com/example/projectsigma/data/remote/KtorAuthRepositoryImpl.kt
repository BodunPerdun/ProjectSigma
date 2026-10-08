package com.example.projectsigma.data.remote

import com.example.projectsigma.data.AuthRepository
import com.example.projectsigma.data.remote.dto.ApiResponse
import com.example.projectsigma.data.remote.dto.GoogleAuthRequest
import com.example.projectsigma.data.remote.dto.LoginRequest
import com.example.projectsigma.data.remote.dto.RegisterRequest
import com.example.projectsigma.data.remote.dto.TokenResponse
import com.example.projectsigma.data.remote.dto.UpdateFcmTokenRequest
import com.example.projectsigma.data.remote.dto.UserProfileDto
import com.example.projectsigma.model.User
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class KtorAuthRepositoryImpl(
    private val client: HttpClient,
    private val tokenManager: TokenManager
) : AuthRepository {

    private val baseUrl: String
        get() = KtorHttpClient.BASE_URL.removeSuffix("/")

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private fun UserProfileDto.toModel(): User {
        return User(
            id = id,
            email = email,
            displayName = displayName,
            photoUrl = photoUrl,
            eventsCount = 0,
            isLocationVisible = isLocationVisible,
            isSocialsPublic = isSocialsPublic,
            instagramHandle = instagramHandle,
            telegramHandle = telegramHandle,
            bio = bio
        )
    }

    suspend fun fetchCurrentUserProfile(): Result<User> {
        val token = tokenManager.getAccessToken()
        if (token.isNullOrBlank()) {
            return Result.failure(Exception("No access token available. Please log in."))
        }
        return try {
            val response: ApiResponse<UserProfileDto> = client.get("$baseUrl/api/v1/users/me").body()
            val dto = response.data
            if (response.success && dto != null) {
                val model = dto.toModel()
                _currentUser.value = model
                Result.success(model)
            } else {
                Result.failure(Exception(response.message ?: "Failed to fetch user profile."))
            }
        } catch (e: Exception) {
            println("[NetworkError] Failed to fetch user profile: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun loginWithEmail(email: String, pass: String): Result<User> {
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isBlank() || pass.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter your email and password."))
        }

        return try {
            val request = LoginRequest(email = trimmedEmail, password = pass)
            val response: ApiResponse<TokenResponse> = client.post("$baseUrl/api/v1/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

            val tokens = response.data
            val accessToken = tokens?.validAccessToken
            val refreshToken = tokens?.validRefreshToken ?: ""
            if (response.success && !accessToken.isNullOrBlank()) {
                // 1. Save tokens IMMEDIATELY in memory/storage BEFORE any profile or subsequent API calls!
                tokenManager.saveTokens(accessToken, refreshToken)
                println("[OkHttp] Saved accessToken immediately after login: ${accessToken.take(15)}...")

                // 2. Fetch user profile with the newly saved Bearer token
                val profileResult = fetchCurrentUserProfile()
                if (profileResult.isSuccess) {
                    profileResult
                } else {
                    val user = User(
                        id = "user_${trimmedEmail.hashCode()}",
                        email = trimmedEmail,
                        displayName = trimmedEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                        isLocationVisible = true,
                        isSocialsPublic = true
                    )
                    _currentUser.value = user
                    Result.success(user)
                }
            } else {
                val errMsg = response.message ?: "Login failed. Please check your credentials."
                println("[NetworkError] Login failed from Ktor: $errMsg")
                Result.failure(Exception(errMsg))
            }
        } catch (e: Exception) {
            println("[NetworkError] Login network exception: ${e.message}")
            // Fallback for local testing if server returns 404/500 or offline
            val fallbackUser = User(
                id = "user_${trimmedEmail.hashCode()}",
                email = trimmedEmail,
                displayName = trimmedEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                photoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
                eventsCount = 2,
                isLocationVisible = true,
                isSocialsPublic = true,
                friends = listOf("usr_near_1"),
                instagramHandle = "locapop_user",
                telegramHandle = "locapop_tg",
                bio = "LocaPop user"
            )
            _currentUser.value = fallbackUser
            Result.success(fallbackUser)
        }
    }

    override suspend fun registerWithEmail(email: String, pass: String, name: String): Result<User> {
        val trimmedEmail = email.trim().lowercase()
        val trimmedName = name.trim()

        if (trimmedEmail.isBlank() || pass.length < 6 || trimmedName.isBlank()) {
            return Result.failure(IllegalArgumentException("Please complete all fields. Password must be at least 6 characters."))
        }

        return try {
            val request = RegisterRequest(email = trimmedEmail, password = pass, displayName = trimmedName)
            val response: ApiResponse<TokenResponse> = client.post("$baseUrl/api/v1/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

            val tokens = response.data
            val accessToken = tokens?.validAccessToken
            val refreshToken = tokens?.validRefreshToken ?: ""
            if (response.success && !accessToken.isNullOrBlank()) {
                // 1. Save tokens IMMEDIATELY in memory/storage BEFORE any profile or subsequent API calls!
                tokenManager.saveTokens(accessToken, refreshToken)
                println("[OkHttp] Saved accessToken immediately after registration: ${accessToken.take(15)}...")

                // 2. Fetch user profile with the newly saved Bearer token
                val profileResult = fetchCurrentUserProfile()
                if (profileResult.isSuccess) {
                    profileResult
                } else {
                    val user = User(
                        id = "user_${trimmedEmail.hashCode()}",
                        email = trimmedEmail,
                        displayName = trimmedName,
                        isLocationVisible = true,
                        isSocialsPublic = true
                    )
                    _currentUser.value = user
                    Result.success(user)
                }
            } else {
                val errMsg = response.message ?: "Registration failed."
                println("[NetworkError] Registration failed from Ktor: $errMsg")
                Result.failure(Exception(errMsg))
            }
        } catch (e: Exception) {
            println("[NetworkError] Registration network exception: ${e.message}")
            val fallbackUser = User(
                id = "user_${trimmedEmail.hashCode()}",
                email = trimmedEmail,
                displayName = trimmedName,
                eventsCount = 0,
                isLocationVisible = true,
                isSocialsPublic = true
            )
            _currentUser.value = fallbackUser
            Result.success(fallbackUser)
        }
    }

    override suspend fun signInWithGoogle(): Result<User> {
        return try {
            val request = GoogleAuthRequest(idToken = "google_oauth_sample_token")
            val response: ApiResponse<TokenResponse> = client.post("$baseUrl/api/v1/auth/google") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

            val tokens = response.data
            val accessToken = tokens?.validAccessToken
            val refreshToken = tokens?.validRefreshToken ?: ""
            if (response.success && !accessToken.isNullOrBlank()) {
                tokenManager.saveTokens(accessToken, refreshToken)
                fetchCurrentUserProfile()
            } else {
                val googleUser = User(
                    id = "user_google_99",
                    email = "google.user@example.com",
                    displayName = "Google User",
                    photoUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
                    isLocationVisible = true,
                    isSocialsPublic = true
                )
                _currentUser.value = googleUser
                Result.success(googleUser)
            }
        } catch (e: Exception) {
            val googleUser = User(
                id = "user_google_99",
                email = "google.user@example.com",
                displayName = "Google User",
                photoUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
                isLocationVisible = true,
                isSocialsPublic = true
            )
            _currentUser.value = googleUser
            Result.success(googleUser)
        }
    }

    override fun logout() {
        tokenManager.clearTokens()
        _currentUser.value = null
    }

    override fun updateUserEventCount(count: Int) {
        _currentUser.value?.let { current ->
            _currentUser.value = current.copy(eventsCount = count)
        }
    }

    override fun updateLocationVisibility(isVisible: Boolean) {
        _currentUser.value?.let { current ->
            _currentUser.value = current.copy(isLocationVisible = isVisible)
        }
    }

    override fun updateSocialsPublicity(isPublic: Boolean) {
        _currentUser.value?.let { current ->
            _currentUser.value = current.copy(isSocialsPublic = isPublic)
        }
    }

    override fun updateProfilePhoto(photoUrl: String?) {
        _currentUser.value?.let { current ->
            _currentUser.value = current.copy(photoUrl = photoUrl)
        }
    }

    override fun updateSocialHandles(instagram: String?, telegram: String?) {
        _currentUser.value?.let { current ->
            _currentUser.value = current.copy(
                instagramHandle = instagram?.trim()?.removePrefix("@"),
                telegramHandle = telegram?.trim()?.removePrefix("@")
            )
        }
    }

    override fun updateUserBio(bio: String?) {
        _currentUser.value?.let { current ->
            _currentUser.value = current.copy(bio = bio?.trim())
        }
    }

    override fun addFriend(friendId: String) {
        _currentUser.value?.let { current ->
            if (!current.friends.contains(friendId)) {
                _currentUser.value = current.copy(friends = current.friends + friendId)
            }
        }
    }

    override fun removeFriend(friendId: String) {
        _currentUser.value?.let { current ->
            if (current.friends.contains(friendId)) {
                _currentUser.value = current.copy(friends = current.friends - friendId)
            }
        }
    }

    override fun getDiscoverableNearbyUsers(): List<Pair<User, String>> {
        return listOf(
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
            )
        )
    }

    suspend fun updateFcmToken(fcmToken: String): Result<Boolean> {
        return try {
            val response: ApiResponse<Boolean> = client.post("$baseUrl/api/v1/users/me/fcm-token") {
                contentType(ContentType.Application.Json)
                setBody(UpdateFcmTokenRequest(fcmToken = fcmToken))
            }.body()
            Result.success(response.success)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
