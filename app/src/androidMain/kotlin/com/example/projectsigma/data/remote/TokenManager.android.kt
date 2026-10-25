package com.example.projectsigma.data.remote

import android.content.Context

actual class TokenManager(context: Context) {

    private val prefs = context.getSharedPreferences("locapop_secure_tokens_prefs", Context.MODE_PRIVATE)

    private var inMemoryAccessToken: String? = prefs.getString("access_token", null)
    private var inMemoryRefreshToken: String? = prefs.getString("refresh_token", null)
    private var inMemoryUserId: String? = prefs.getString("user_id", null)

    actual fun getAccessToken(): String? {
        val token = inMemoryAccessToken ?: prefs.getString("access_token", null)
        if (token != null && (token.isBlank() || token == "null" || token.startsWith("token_") || !token.contains("."))) {
            clearTokens()
            return null
        }
        return token
    }

    actual fun getRefreshToken(): String? {
        val refresh = inMemoryRefreshToken ?: prefs.getString("refresh_token", null)
        if (refresh != null && (refresh.isBlank() || refresh == "null")) {
            return null
        }
        return refresh
    }

    actual fun getUserId(): String? {
        return inMemoryUserId ?: prefs.getString("user_id", null)
    }

    actual fun saveTokens(accessToken: String, refreshToken: String, userId: String) {
        if (accessToken.isBlank() || accessToken == "null" || accessToken.startsWith("token_") || !accessToken.contains(".")) {
            return
        }
        inMemoryAccessToken = accessToken
        inMemoryRefreshToken = refreshToken
        if (userId.isNotBlank()) {
            inMemoryUserId = userId
        }
        prefs.edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .apply {
                if (userId.isNotBlank()) {
                    putString("user_id", userId)
                }
            }
            .apply()
    }

    actual fun clearTokens() {
        inMemoryAccessToken = null
        inMemoryRefreshToken = null
        inMemoryUserId = null
        prefs.edit().clear().apply()
    }
}
