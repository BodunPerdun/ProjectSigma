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
import com.example.projectsigma.data.local.PersistentAuthRepositoryImpl
import com.example.projectsigma.data.local.PersistentEventsRepositoryImpl
import com.example.projectsigma.data.local.PersistentFriendRequestServiceImpl
import com.example.projectsigma.data.local.PersistentNotificationsRepositoryImpl
import com.example.projectsigma.i18n.AppLanguageManager
import com.example.projectsigma.notification.NotificationHelper
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

        val persistentEventsRepository = PersistentEventsRepositoryImpl(applicationContext)
        val persistentAuthRepository = PersistentAuthRepositoryImpl(applicationContext)
        val persistentNotificationsRepository = PersistentNotificationsRepositoryImpl(applicationContext)
        val persistentFriendRequestService = PersistentFriendRequestServiceImpl(
            context = applicationContext,
            authRepository = persistentAuthRepository,
            notificationsRepository = persistentNotificationsRepository
        )

        setContent {
            App(
                eventsRepository = persistentEventsRepository,
                authRepository = persistentAuthRepository,
                notificationsRepository = persistentNotificationsRepository,
                friendRequestService = persistentFriendRequestService
            )
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
