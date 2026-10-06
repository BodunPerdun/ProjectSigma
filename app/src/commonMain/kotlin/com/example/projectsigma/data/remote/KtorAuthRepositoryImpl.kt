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
        return try {
            val response: ApiResponse<UserProfileDto> = client.get("${KtorHttpClient.BASE_URL}/api/v1/users/me").body()
            val dto = response.data
            if (response.success && dto != null) {
                val model = dto.toModel()
                _currentUser.value = model
                Result.success(model)
            } else {
                Result.failure(Exception(response.message ?: "Failed to fetch user profile."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithEmail(email: String, pass: String): Result<User> {
        return try {
            val request = LoginRequest(email = email.trim(), password = pass)
            val response: ApiResponse<TokenResponse> = client.post("${KtorHttpClient.BASE_URL}/api/v1/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

            val tokens = response.data
            if (response.success && tokens != null) {
                tokenManager.saveTokens(tokens.accessToken, tokens.refreshToken)
                fetchCurrentUserProfile()
            } else {
                Result.failure(Exception(response.message ?: "Login failed."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun registerWithEmail(email: String, pass: String, name: String): Result<User> {
        return try {
            val request = RegisterRequest(email = email.trim(), password = pass, displayName = name.trim())
            val response: ApiResponse<TokenResponse> = client.post("${KtorHttpClient.BASE_URL}/api/v1/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

            val tokens = response.data
            if (response.success && tokens != null) {
                tokenManager.saveTokens(tokens.accessToken, tokens.refreshToken)
                fetchCurrentUserProfile()
            } else {
                Result.failure(Exception(response.message ?: "Registration failed."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(): Result<User> {
        return try {
            val request = GoogleAuthRequest(idToken = "google_oauth_sample_token")
            val response: ApiResponse<TokenResponse> = client.post("${KtorHttpClient.BASE_URL}/api/v1/auth/google") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

            val tokens = response.data
            if (response.success && tokens != null) {
                tokenManager.saveTokens(tokens.accessToken, tokens.refreshToken)
                fetchCurrentUserProfile()
            } else {
                Result.failure(Exception(response.message ?: "Google auth failed."))
            }
        } catch (e: Exception) {
            Result.failure(e)
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
        return emptyList()
    }

    suspend fun updateFcmToken(fcmToken: String): Result<Boolean> {
        return try {
            val response: ApiResponse<Boolean> = client.post("${KtorHttpClient.BASE_URL}/api/v1/users/me/fcm-token") {
                contentType(ContentType.Application.Json)
                setBody(UpdateFcmTokenRequest(fcmToken = fcmToken))
            }.body()
            Result.success(response.success)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
