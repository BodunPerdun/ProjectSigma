package com.example.projectsigma.data.remote

actual class TokenManager {
    private var accessToken: String? = null
    private var refreshToken: String? = null
    private var userId: String? = null

    actual fun getAccessToken(): String? {
        if (accessToken != null && (accessToken!!.isBlank() || accessToken == "null" || !accessToken!!.contains("."))) {
            clearTokens()
            return null
        }
        return accessToken
    }

    actual fun getRefreshToken(): String? {
        return refreshToken
    }

    actual fun getUserId(): String? {
        return userId
    }

    actual fun saveTokens(accessToken: String, refreshToken: String, userId: String) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
        this.userId = userId
    }

    actual fun clearTokens() {
        accessToken = null
        refreshToken = null
        userId = null
    }
}
