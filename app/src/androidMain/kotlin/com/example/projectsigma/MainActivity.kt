package com.example.projectsigma

import android.accounts.AccountManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.example.projectsigma.data.remote.KtorAuthRepositoryImpl
import com.example.projectsigma.data.remote.KtorEventsRepositoryImpl
import com.example.projectsigma.data.remote.KtorFriendRequestServiceImpl
import com.example.projectsigma.data.remote.KtorHttpClient
import com.example.projectsigma.data.remote.KtorNotificationsRepositoryImpl
import com.example.projectsigma.data.remote.MapWebSocketClient
import com.example.projectsigma.data.remote.TokenManager
import com.example.projectsigma.i18n.AppLanguageManager
import com.example.projectsigma.notification.NotificationHelper
import com.example.projectsigma.theme.AppThemeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        currentActivityContext = applicationContext

        NotificationHelper.createNotificationChannel(applicationContext)
        requestNotificationPermission()
        restoreUserPreferences()

        val tokenManager = TokenManager(applicationContext)
        val ktorClient = KtorHttpClient.createClient(tokenManager)
        val webSocketClient = MapWebSocketClient(ktorClient)

        val ktorAuthRepository = KtorAuthRepositoryImpl(ktorClient, tokenManager)
        val ktorEventsRepository = KtorEventsRepositoryImpl(ktorClient, webSocketClient)
        val ktorNotificationsRepository = KtorNotificationsRepositoryImpl(ktorClient)
        val ktorFriendRequestService = KtorFriendRequestServiceImpl(ktorClient)

        val googleAccountPickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val accountName = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                if (!accountName.isNullOrBlank()) {
                    val formattedName = accountName.substringBefore("@")
                        .replace(".", " ")
                        .replace("_", " ")
                        .split(" ")
                        .joinToString(" ") { word -> word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }

                    Log.d("MainActivity", "Google Account selected: $accountName")
                    CoroutineScope(Dispatchers.Main).launch {
                        ktorAuthRepository.signInWithGoogle(
                            email = accountName,
                            displayName = formattedName,
                            photoUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150"
                        )
                    }
                }
            }
        }

        globalAccountPickerLauncher = { intent ->
            googleAccountPickerLauncher.launch(intent)
        }

        webSocketClient.connect()

        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                KtorHttpClient.checkServerHealth(ktorClient)
            }.onFailure { e ->
                Log.w("Network", "Server health check failed: ${e.message}")
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

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val launcher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (!isGranted) {
                    Log.w("MainActivity", "POST_NOTIFICATIONS permission denied")
                }
            }
            launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun restoreUserPreferences() {
        val langFile = File(applicationContext.filesDir, "app_language.txt")
        val savedLangCode = if (langFile.exists()) langFile.readText().trim() else null
        AppLanguageManager.initPersistence(savedLangCode) { newLang ->
            runCatching { langFile.writeText(newLang.code) }
        }

        val themeFile = File(applicationContext.filesDir, "app_theme.txt")
        val savedThemeState = if (themeFile.exists()) themeFile.readText().trim().toBooleanStrictOrNull() else null
        AppThemeManager.initPersistence(savedThemeState) { isDark ->
            runCatching { themeFile.writeText(isDark.toString()) }
        }
    }
}
