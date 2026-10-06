package com.example.projectsigma.data.remote

import com.example.projectsigma.data.remote.dto.MapWebSocketMessage
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class MapWebSocketClient(
    private val client: HttpClient
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _messages = MutableSharedFlow<MapWebSocketMessage>()
    val messages: SharedFlow<MapWebSocketMessage> = _messages.asSharedFlow()

    private var isConnected = false

    fun connect() {
        if (isConnected) return
        scope.launch {
            try {
                val session = client.webSocketSession(KtorHttpClient.WS_URL)
                isConnected = true

                for (frame in session.incoming) {
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        try {
                            val wsMessage = KtorHttpClient.jsonConfig.decodeFromString<MapWebSocketMessage>(text)
                            _messages.emit(wsMessage)
                        } catch (e: Exception) {
                            // parse error ignore
                        }
                    }
                }
            } catch (e: Exception) {
                isConnected = false
            } finally {
                isConnected = false
            }
        }
    }
}
