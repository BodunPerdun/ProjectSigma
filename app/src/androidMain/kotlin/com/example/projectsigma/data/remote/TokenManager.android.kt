package com.example.projectsigma.data.remote

import android.content.Context

actual class TokenManager(context: Context) {

    private val prefs = context.getSharedPreferences("locapop_secure_tokens_prefs", Context.MODE_PRIVATE)

    private var inMemoryAccessToken: String? = prefs.getString("access_token", null)
    private var inMemoryRefreshToken: String? = prefs.getString("refresh_token", null)

    actual fun getAccessToken(): String? {
        return inMemoryAccessToken ?: prefs.getString("access_token", null)
    }

    actual fun getRefreshToken(): String? {
        return inMemoryRefreshToken ?: prefs.getString("refresh_token", null)
    }

    actual fun saveTokens(accessToken: String, refreshToken: String) {
        inMemoryAccessToken = accessToken
        inMemoryRefreshToken = refreshToken
        prefs.edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .apply()
    }

    actual fun clearTokens() {
        inMemoryAccessToken = null
        inMemoryRefreshToken = null
        prefs.edit().clear().apply()
    }
}
