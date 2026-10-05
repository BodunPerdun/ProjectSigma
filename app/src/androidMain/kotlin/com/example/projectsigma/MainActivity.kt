package com.example.projectsigma

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.projectsigma.data.local.PersistentEventsRepositoryImpl

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val persistentEventsRepository = PersistentEventsRepositoryImpl(applicationContext)
        setContent {
            App(eventsRepository = persistentEventsRepository)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
