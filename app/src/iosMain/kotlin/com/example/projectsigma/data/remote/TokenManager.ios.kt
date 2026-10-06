package com.example.projectsigma.data.remote

actual class TokenManager {
    private var accessTokenMemory: String? = null
    private var refreshTokenMemory: String? = null

    actual fun getAccessToken(): String? = accessTokenMemory
    actual fun getRefreshToken(): String? = refreshTokenMemory

    actual fun saveTokens(accessToken: String, refreshToken: String) {
        accessTokenMemory = accessToken
        refreshTokenMemory = refreshToken
    }

    actual fun clearTokens() {
        accessTokenMemory = null
        refreshTokenMemory = null
    }
}
