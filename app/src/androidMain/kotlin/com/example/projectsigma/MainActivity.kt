package com.example.projectsigma

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.projectsigma.data.remote.KtorAuthRepositoryImpl
import com.example.projectsigma.data.remote.KtorEventsRepositoryImpl
import com.example.projectsigma.data.remote.KtorFriendRequestServiceImpl
import com.example.projectsigma.data.remote.KtorHttpClient
import com.example.projectsigma.data.remote.KtorNotificationsRepositoryImpl
import com.example.projectsigma.data.remote.MapWebSocketClient
import com.example.projectsigma.data.remote.TokenManager
import com.example.projectsigma.i18n.AppLanguageManager
import com.example.projectsigma.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        super.onCreate(savedInstanceState)

        // Initialize notification channel
        NotificationHelper.createNotificationChannel(applicationContext)

        // Request POST_NOTIFICATIONS permission on Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val requestPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    Log.d("MainActivity", "Notification permission granted.")
                } else {
                    Log.w("MainActivity", "Notification permission denied.")
                }
            }
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        // Initialize persistent language manager (Defaults to ENGLISH on clean install)
        val langFile = File(applicationContext.filesDir, "app_language.txt")
        val savedCode = if (langFile.exists()) langFile.readText().trim() else null

        AppLanguageManager.initPersistence(savedCode) { newLang ->
            try {
                langFile.writeText(newLang.code)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error saving language: ${e.message}")
            }
        }

        // Initialize Ktor HTTP & WebSockets Network Repositories
        val tokenManager = TokenManager(applicationContext)
        val ktorClient = KtorHttpClient.createClient(tokenManager)

        val ktorAuthRepository = KtorAuthRepositoryImpl(ktorClient, tokenManager)
        val ktorEventsRepository = KtorEventsRepositoryImpl(ktorClient)
        val ktorNotificationsRepository = KtorNotificationsRepositoryImpl(ktorClient)
        val ktorFriendRequestService = KtorFriendRequestServiceImpl(ktorClient)

        // Connect real-time WebSocket client
        val webSocketClient = MapWebSocketClient(ktorClient)
        webSocketClient.connect()

        // Perform server health check on app startup in background Dispatchers.IO context
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val isHealthy = KtorHttpClient.checkServerHealth(ktorClient)
                if (isHealthy) {
                    Log.d("OkHttp", "Server Health Check OK at ${KtorHttpClient.BASE_URL}api/v1/health")
                } else {
                    Log.w("NetworkError", "Server health check failed at ${KtorHttpClient.BASE_URL}api/v1/health")
                }
            } catch (e: Exception) {
                Log.e("NetworkError", "Failed to reach server at ${KtorHttpClient.BASE_URL}api/v1/health", e)
            }
        }

        setContent {
            App(
                eventsRepository = ktorEventsRepository,
                authRepository = ktorAuthRepository,
                notificationsRepository = ktorNotificationsRepository,
                friendRequestService = ktorFriendRequestService
            )
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
