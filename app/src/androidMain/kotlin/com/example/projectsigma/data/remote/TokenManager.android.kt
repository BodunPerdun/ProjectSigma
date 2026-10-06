package com.example.projectsigma.data.remote

import android.content.Context

actual class TokenManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("locapop_secure_tokens_prefs", Context.MODE_PRIVATE)

    actual fun getAccessToken(): String? {
        return prefs.getString("access_token", null)
    }

    actual fun getRefreshToken(): String? {
        return prefs.getString("refresh_token", null)
    }

    actual fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .apply()
    }

    actual fun clearTokens() {
        prefs.edit().clear().apply()
    }
}
