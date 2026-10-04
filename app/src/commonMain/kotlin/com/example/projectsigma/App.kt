package com.example.projectsigma

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.projectsigma.data.AuthRepository
import com.example.projectsigma.data.EventsRepository
import com.example.projectsigma.data.LocalAuthRepositoryImpl
import com.example.projectsigma.data.LocalEventsRepositoryImpl
import com.example.projectsigma.ui.auth.AuthScreen
import com.example.projectsigma.ui.main.MainScreen
import com.example.projectsigma.ui.splash.SplashScreen
import com.example.projectsigma.viewmodel.AuthViewModel
import com.example.projectsigma.viewmodel.MainViewModel

object AppDependencies {
    val authRepository: AuthRepository = LocalAuthRepositoryImpl()
    val eventsRepository: EventsRepository = LocalEventsRepositoryImpl()
}

@Composable
fun App() {
    var isSplashVisible by remember { mutableStateOf(true) }

    val authViewModel = remember { AuthViewModel(AppDependencies.authRepository) }
    val mainViewModel = remember { MainViewModel(AppDependencies.eventsRepository, AppDependencies.authRepository) }

    val currentUser by authViewModel.currentUser.collectAsState()

    MaterialTheme {
        Surface {
            if (isSplashVisible) {
                SplashScreen(onSplashFinished = { isSplashVisible = false })
            } else if (currentUser == null) {
                AuthScreen(authViewModel = authViewModel)
            } else {
                MainScreen(
                    mainViewModel = mainViewModel,
                    authViewModel = authViewModel
                )
            }
        }
    }
}
