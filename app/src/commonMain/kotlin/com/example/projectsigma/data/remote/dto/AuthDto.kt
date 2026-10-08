package com.example.projectsigma.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class GoogleAuthRequest(
    val idToken: String
)

@Serializable
data class TokenResponse(
    val accessToken: String? = null,
    @SerialName("access_token") val accessTokenSnake: String? = null,
    val refreshToken: String? = null,
    @SerialName("refresh_token") val refreshTokenSnake: String? = null,
    val tokenType: String = "Bearer"
) {
    val validAccessToken: String
        get() = accessToken ?: accessTokenSnake ?: ""

    val validRefreshToken: String
        get() = refreshToken ?: refreshTokenSnake ?: validAccessToken
}
