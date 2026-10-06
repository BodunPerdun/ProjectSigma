package com.example.projectsigma.data.remote.dto

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
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer"
)
