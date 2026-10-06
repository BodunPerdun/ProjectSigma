package com.example.projectsigma

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.projectsigma.data.local.PersistentAuthRepositoryImpl
import com.example.projectsigma.data.local.PersistentEventsRepositoryImpl
import com.example.projectsigma.i18n.AppLanguageManager
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        setContent {
            App(
                eventsRepository = persistentEventsRepository,
                authRepository = persistentAuthRepository
            )
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
