package com.example.projectsigma.data.remote

import com.example.projectsigma.data.remote.dto.ApiResponse
import com.example.projectsigma.data.remote.dto.TokenResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

object KtorHttpClient {

    var BASE_URL = "https://sliceable-overboard-kitchen.ngrok-free.dev"
    var WS_URL = "ws://192.168.0.101:8080/api/v1/ws/map"

    val jsonConfig = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        prettyPrint = true
    }

    fun createClient(tokenManager: TokenManager? = null): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                json(jsonConfig)
            }

            install(WebSockets)

            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println("[OkHttp] $message")
                    }
                }
                level = LogLevel.BODY
            }

            defaultRequest {
                contentType(ContentType.Application.Json)
            }

            if (tokenManager != null) {
                install(Auth) {
                    bearer {
                        // Preemptively send Bearer token on 1st request for protected routes (without waiting for 401 challenge)
                        sendWithoutRequest { request: HttpRequestBuilder ->
                            val path = request.url.build().encodedPath
                            !path.contains("/auth/") && !path.contains("/health")
                        }

                        loadTokens {
                            val access = tokenManager.getAccessToken()
                            val refresh = tokenManager.getRefreshToken() ?: ""
                            if (!access.isNullOrBlank() && access != "null") {
                                BearerTokens(access, refresh)
                            } else null
                        }

                        refreshTokens {
                            val refresh = tokenManager.getRefreshToken()
                            if (refresh.isNullOrBlank() || refresh == "null") return@refreshTokens null
                            try {
                                val baseUrl = BASE_URL.removeSuffix("/")
                                val response: ApiResponse<TokenResponse> = client.post("$baseUrl/api/v1/auth/refresh") {
                                    markAsRefreshTokenRequest()
                                    contentType(ContentType.Application.Json)
                                    setBody(mapOf("refreshToken" to refresh))
                                }.body()

                                val newTokens = response.data
                                val newAccess = newTokens?.validAccessToken
                                val newRefresh = newTokens?.validRefreshToken ?: ""
                                val userId = newTokens?.user?.id ?: tokenManager.getUserId() ?: ""
                                if (response.success && !newAccess.isNullOrBlank()) {
                                    tokenManager.saveTokens(newAccess, newRefresh, userId)
                                    BearerTokens(newAccess, newRefresh)
                                } else {
                                    tokenManager.clearTokens()
                                    null
                                }
                            } catch (e: Exception) {
                                tokenManager.clearTokens()
                                null
                            }
                        }
                    }
                }
            }
        }
    }

    suspend fun checkServerHealth(client: HttpClient): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val baseUrl = BASE_URL.removeSuffix("/")
                val response: HttpResponse = client.get("$baseUrl/api/v1/health")
                val text = response.bodyAsText()
                println("[OkHttp] Server Health Check Success: $text")
                true
            } catch (e: Exception) {
                println("[NetworkError] Failed to reach server at ${BASE_URL}api/v1/health: ${e.message}")
                false
            }
        }
    }
}
