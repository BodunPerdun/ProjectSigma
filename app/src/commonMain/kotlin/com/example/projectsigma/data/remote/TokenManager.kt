package com.example.projectsigma.data.remote

expect class TokenManager {
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun saveTokens(accessToken: String, refreshToken: String)
    fun clearTokens()
}
