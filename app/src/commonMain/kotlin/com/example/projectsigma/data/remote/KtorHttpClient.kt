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
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

object KtorHttpClient {

    var BASE_URL = "http://192.168.0.101:8080/"
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
                        loadTokens {
                            val access = tokenManager.getAccessToken()
                            val refresh = tokenManager.getRefreshToken()
                            if (!access.isNullOrBlank() && !refresh.isNullOrBlank()) {
                                BearerTokens(access, refresh)
                            } else null
                        }

                        refreshTokens {
                            try {
                                val oldRefresh = tokenManager.getRefreshToken() ?: return@refreshTokens null
                                val response: ApiResponse<TokenResponse> = client.post("${BASE_URL}api/v1/auth/refresh") {
                                    contentType(ContentType.Application.Json)
                                    setBody(mapOf("refreshToken" to oldRefresh))
                                }.body()

                                val newTokens = response.data
                                if (response.success && newTokens != null) {
                                    tokenManager.saveTokens(newTokens.accessToken, newTokens.refreshToken)
                                    BearerTokens(newTokens.accessToken, newTokens.refreshToken)
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
                val response = client.get("${BASE_URL}api/v1/health")
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
