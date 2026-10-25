package com.example.projectsigma.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class GoogleUserProfile(
    val name: String,
    val avatarUrl: String,
    val email: String,
    val idToken: String = ""
)

object GoogleOAuthAppAuthHelper {

    const val CLIENT_ID = "692599024856-ikoc5a4le4uub5aq76smifhb0nqfp3g3.apps.googleusercontent.com"
    const val REDIRECT_URI = "com.googleusercontent.apps.692599024856-ikoc5a4le4uub5aq76smifhb0nqfp3g3:/oauth2redirect"

    private val serviceConfiguration = AuthorizationServiceConfiguration(
        Uri.parse("https://accounts.google.com/o/oauth2/v2/auth"),
        Uri.parse("https://oauth2.googleapis.com/token")
    )

    fun createAuthIntent(context: Context): Intent {
        val authService = AuthorizationService(context)
        return try {
            val authRequest = AuthorizationRequest.Builder(
                serviceConfiguration,
                CLIENT_ID,
                ResponseTypeValues.CODE,
                Uri.parse(REDIRECT_URI)
            )
                .setScopes("openid", "profile", "email")
                .setPrompt("select_account consent")
                .build()

            authService.getAuthorizationRequestIntent(authRequest)
        } finally {
            authService.dispose()
        }
    }

    suspend fun handleAuthResult(
        context: Context,
        data: Intent,
        onProfileReceived: (GoogleUserProfile) -> Unit,
        onError: (String) -> Unit
    ) {
        val response = AuthorizationResponse.fromIntent(data)
        val ex = AuthorizationException.fromIntent(data)

        if (ex != null) {
            Log.e("AppAuth", "OAuth Authorization error: ${ex.message}")
            onError(ex.message ?: "Authorization failed")
            return
        }

        if (response != null) {
            val authService = AuthorizationService(context)
            try {
                val tokenExchangeRequest = response.createTokenExchangeRequest()
                authService.performTokenRequest(tokenExchangeRequest) { tokenResponse, tokenException ->
                    if (tokenException != null) {
                        Log.e("AppAuth", "Token Exchange Error: ${tokenException.message}")
                        onError(tokenException.message ?: "Token exchange failed")
                        return@performTokenRequest
                    }

                    if (tokenResponse != null) {
                        val accessToken = tokenResponse.accessToken
                        val idToken = tokenResponse.idToken ?: ""

                        Log.d("AppAuth", "Token Exchange Success. AccessToken length: ${accessToken?.length}")

                        CoroutineScope(Dispatchers.IO).launch {
                            val profile = parseUserProfile(idToken, accessToken)
                            withContext(Dispatchers.Main) {
                                if (profile != null) {
                                    onProfileReceived(profile.copy(idToken = idToken))
                                } else {
                                    onError("Failed to parse Google user profile")
                                }
                            }
                        }
                    }
                }
            } finally {
                authService.dispose()
            }
        } else {
            onError("No authorization response received")
        }
    }

    private suspend fun parseUserProfile(idToken: String?, accessToken: String?): GoogleUserProfile? {
        return withContext(Dispatchers.IO) {
            // First attempt: Parse ID Token JWT Payload
            if (!idToken.isNullOrBlank()) {
                try {
                    val parts = idToken.split(".")
                    if (parts.size >= 2) {
                        val payloadJson = String(
                            Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING),
                            Charsets.UTF_8
                        )
                        val json = JSONObject(payloadJson)

                        val email = json.optString("email", "")
                        var name = json.optString("name", "")
                        if (name.isBlank()) {
                            name = json.optString("given_name", "")
                        }
                        if (name.isBlank() && email.isNotBlank()) {
                            name = email.substringBefore("@")
                                .replace(".", " ")
                                .replace("_", " ")
                                .split(" ")
                                .joinToString(" ") { word -> word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }
                        }
                        val picture = json.optString("picture", "")

                        if (email.isNotBlank()) {
                            Log.d("AppAuth", "Extracted Profile from ID Token: $name ($email), Avatar: $picture")
                            return@withContext GoogleUserProfile(
                                name = name.ifBlank { "Google User" },
                                avatarUrl = picture.ifBlank { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150" },
                                email = email,
                                idToken = idToken
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.w("AppAuth", "Error parsing ID Token JWT payload: ${e.message}")
                }
            }

            // Fallback attempt: GET request to https://www.googleapis.com/oauth2/v3/userinfo
            if (!accessToken.isNullOrBlank()) {
                try {
                    val url = URL("https://www.googleapis.com/oauth2/v3/userinfo")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "GET"
                    conn.setRequestProperty("Authorization", "Bearer $accessToken")

                    if (conn.responseCode == 200) {
                        val reader = BufferedReader(InputStreamReader(conn.inputStream))
                        val responseText = reader.readText()
                        reader.close()

                        val json = JSONObject(responseText)
                        val email = json.optString("email", "")
                        var name = json.optString("name", "")
                        if (name.isBlank()) {
                            name = json.optString("given_name", "")
                        }
                        if (name.isBlank() && email.isNotBlank()) {
                            name = email.substringBefore("@")
                                .replace(".", " ")
                                .replace("_", " ")
                                .split(" ")
                                .joinToString(" ") { word -> word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }
                        }
                        val picture = json.optString("picture", "")

                        Log.d("AppAuth", "Fetched Profile from Google UserInfo endpoint: $name ($email)")
                        return@withContext GoogleUserProfile(
                            name = name.ifBlank { "Google User" },
                            avatarUrl = picture.ifBlank { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150" },
                            email = email,
                            idToken = idToken ?: ""
                        )
                    }
                } catch (e: Exception) {
                    Log.e("AppAuth", "Error fetching Google userinfo endpoint: ${e.message}")
                }
            }

            null
        }
    }
}
