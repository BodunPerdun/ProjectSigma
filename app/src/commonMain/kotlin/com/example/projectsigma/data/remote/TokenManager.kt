package com.example.projectsigma.data.remote

expect class TokenManager {
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun getUserId(): String?
    fun saveTokens(accessToken: String, refreshToken: String, userId: String = "")
    fun clearTokens()
}
