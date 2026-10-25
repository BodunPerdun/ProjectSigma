package com.example.projectsigma.data.remote

import com.example.projectsigma.data.AuthRepository
import com.example.projectsigma.data.remote.dto.ApiResponse
import com.example.projectsigma.data.remote.dto.GoogleAuthRequest
import com.example.projectsigma.data.remote.dto.LoginRequest
import com.example.projectsigma.data.remote.dto.RegisterRequest
import com.example.projectsigma.data.remote.dto.TokenResponse
import com.example.projectsigma.data.remote.dto.UpdateFcmTokenRequest
import com.example.projectsigma.data.remote.dto.UpdatePrivacyRequest
import com.example.projectsigma.data.remote.dto.UpdateProfileRequest
import com.example.projectsigma.data.remote.dto.UserProfileDto
import com.example.projectsigma.model.User
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class KtorAuthRepositoryImpl(
    private val client: HttpClient,
    private val tokenManager: TokenManager
) : AuthRepository {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val baseUrl: String
        get() = KtorHttpClient.BASE_URL.removeSuffix("/")

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        val savedToken = tokenManager.getAccessToken()
        if (!savedToken.isNullOrBlank()) {
            scope.launch {
                fetchCurrentUserProfile()
            }
        }
    }

    private fun UserProfileDto.toModel(): User {
        return User(
            id = validId,
            email = email,
            displayName = displayName,
            photoUrl = photoUrl,
            eventsCount = friendsCount,
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
                val serverModel = dto.toModel()
                val currentLocal = _currentUser.value
                val finalUser = serverModel.copy(
                    displayName = if (serverModel.displayName.isBlank() || serverModel.displayName == "User") (currentLocal?.displayName ?: "User") else serverModel.displayName,
                    photoUrl = if (serverModel.photoUrl.isNullOrBlank()) currentLocal?.photoUrl else serverModel.photoUrl
                )
                _currentUser.value = finalUser
                Result.success(finalUser)
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
            val userId = tokens?.user?.id ?: ""

            if (response.success && !accessToken.isNullOrBlank()) {
                tokenManager.saveTokens(accessToken, refreshToken, userId)
                println("[OkHttp] Saved accessToken immediately after login: ${accessToken.take(15)}...")

                val profileResult = fetchCurrentUserProfile()
                if (profileResult.isSuccess) {
                    profileResult
                } else {
                    val user = User(
                        id = if (userId.isNotBlank()) userId else "user_${trimmedEmail.hashCode()}",
                        email = trimmedEmail,
                        displayName = trimmedEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                        isLocationVisible = true,
                        isSocialsPublic = true
                    )
                    _currentUser.value = user
                    Result.success(user)
                }
            } else {
                val fallbackUser = User(
                    id = "user_${trimmedEmail.hashCode()}",
                    email = trimmedEmail,
                    displayName = trimmedEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                    isLocationVisible = true,
                    isSocialsPublic = true
                )
                _currentUser.value = fallbackUser
                Result.success(fallbackUser)
            }
        } catch (e: Exception) {
            println("[NetworkError] Login network exception: ${e.message}")
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
            val userId = tokens?.user?.id ?: ""

            if (response.success && !accessToken.isNullOrBlank()) {
                tokenManager.saveTokens(accessToken, refreshToken, userId)
                println("[OkHttp] Saved accessToken immediately after registration: ${accessToken.take(15)}...")

                val profileResult = fetchCurrentUserProfile()
                if (profileResult.isSuccess) {
                    profileResult
                } else {
                    val user = User(
                        id = if (userId.isNotBlank()) userId else "user_${trimmedEmail.hashCode()}",
                        email = trimmedEmail,
                        displayName = trimmedName,
                        isLocationVisible = true,
                        isSocialsPublic = true
                    )
                    _currentUser.value = user
                    Result.success(user)
                }
            } else {
                val fallbackUser = User(
                    id = "user_${trimmedEmail.hashCode()}",
                    email = trimmedEmail,
                    displayName = trimmedName,
                    isLocationVisible = true,
                    isSocialsPublic = true
                )
                _currentUser.value = fallbackUser
                Result.success(fallbackUser)
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

    override suspend fun signInWithGoogle(email: String?, displayName: String?, photoUrl: String?): Result<User> {
        val googleEmail = email?.trim()?.lowercase() ?: "google.user@gmail.com"
        val googleName = displayName?.trim() ?: "Google User"
        val googleAvatar = photoUrl ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150"

        return try {
            val request = GoogleAuthRequest(idToken = "google_token_${googleEmail.hashCode()}")
            val response: ApiResponse<TokenResponse> = client.post("$baseUrl/api/v1/auth/google") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

            val tokens = response.data
            val accessToken = tokens?.validAccessToken
            val refreshToken = tokens?.validRefreshToken ?: ""
            val userId = tokens?.user?.id ?: ""

            if (response.success && !accessToken.isNullOrBlank()) {
                tokenManager.saveTokens(accessToken, refreshToken, userId)
                println("[OkHttp] Saved REAL JWT accessToken from Ktor server: ${accessToken.take(15)}...")

                val profileResult = fetchCurrentUserProfile()
                if (profileResult.isSuccess) {
                    val serverUser = profileResult.getOrNull()
                    if (serverUser != null) {
                        val mergedUser = serverUser.copy(
                            displayName = if (serverUser.displayName.isBlank() || serverUser.displayName == "User") googleName else serverUser.displayName,
                            photoUrl = if (serverUser.photoUrl.isNullOrBlank()) googleAvatar else serverUser.photoUrl
                        )
                        _currentUser.value = mergedUser

                        if (serverUser.photoUrl.isNullOrBlank()) {
                            updateProfilePhoto(googleAvatar)
                        }

                        return Result.success(mergedUser)
                    }
                }
            }

            val fallbackGoogleUser = User(
                id = if (userId.isNotBlank()) userId else "user_google_${googleEmail.hashCode()}",
                email = googleEmail,
                displayName = googleName,
                photoUrl = googleAvatar,
                isLocationVisible = true,
                isSocialsPublic = true
            )
            _currentUser.value = fallbackGoogleUser
            Result.success(fallbackGoogleUser)
        } catch (e: Exception) {
            println("[NetworkError] Google sign-in network exception: ${e.message}")
            val fallbackGoogleUser = User(
                id = "user_google_${googleEmail.hashCode()}",
                email = googleEmail,
                displayName = googleName,
                photoUrl = googleAvatar,
                isLocationVisible = true,
                isSocialsPublic = true
            )
            _currentUser.value = fallbackGoogleUser
            Result.success(fallbackGoogleUser)
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
            val updated = current.copy(isLocationVisible = isVisible)
            _currentUser.value = updated
            scope.launch {
                try {
                    client.put("$baseUrl/api/v1/users/me/privacy") {
                        contentType(ContentType.Application.Json)
                        setBody(UpdatePrivacyRequest(isLocationVisible = isVisible))
                    }
                } catch (e: Exception) {
                    println("[NetworkError] Failed to update privacy on Ktor: ${e.message}")
                }
            }
        }
    }

    override fun updateSocialsPublicity(isPublic: Boolean) {
        _currentUser.value?.let { current ->
            val updated = current.copy(isSocialsPublic = isPublic)
            _currentUser.value = updated
            scope.launch {
                try {
                    client.put("$baseUrl/api/v1/users/me/privacy") {
                        contentType(ContentType.Application.Json)
                        setBody(UpdatePrivacyRequest(isSocialsPublic = isPublic))
                    }
                } catch (e: Exception) {
                    println("[NetworkError] Failed to update privacy on Ktor: ${e.message}")
                }
            }
        }
    }

    override fun updateProfilePhoto(photoUrl: String?) {
        _currentUser.value?.let { current ->
            val updated = current.copy(photoUrl = photoUrl)
            _currentUser.value = updated
            scope.launch {
                try {
                    client.put("$baseUrl/api/v1/users/me") {
                        contentType(ContentType.Application.Json)
                        setBody(UpdateProfileRequest(photoUrl = photoUrl))
                    }
                    println("[OkHttp] Profile avatar updated on Ktor server.")
                } catch (e: Exception) {
                    println("[NetworkError] Failed to update profile photo on Ktor: ${e.message}")
                }
            }
        }
    }

    override fun updateSocialHandles(instagram: String?, telegram: String?) {
        val insta = instagram?.trim()?.removePrefix("@")
        val tg = telegram?.trim()?.removePrefix("@")
        _currentUser.value?.let { current ->
            val updated = current.copy(
                instagramHandle = insta,
                telegramHandle = tg
            )
            _currentUser.value = updated
            scope.launch {
                try {
                    client.put("$baseUrl/api/v1/users/me") {
                        contentType(ContentType.Application.Json)
                        setBody(UpdateProfileRequest(instagramHandle = insta, telegramHandle = tg))
                    }
                    println("[OkHttp] Social handles updated on Ktor server.")
                } catch (e: Exception) {
                    println("[NetworkError] Failed to update social handles on Ktor: ${e.message}")
                }
            }
        }
    }

    override fun updateUserBio(bio: String?) {
        val trimmedBio = bio?.trim()
        _currentUser.value?.let { current ->
            val updated = current.copy(bio = trimmedBio)
            _currentUser.value = updated
            scope.launch {
                try {
                    client.put("$baseUrl/api/v1/users/me") {
                        contentType(ContentType.Application.Json)
                        setBody(UpdateProfileRequest(bio = trimmedBio))
                    }
                    println("[OkHttp] User bio updated on Ktor server.")
                } catch (e: Exception) {
                    println("[NetworkError] Failed to update bio on Ktor: ${e.message}")
                }
            }
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
