package com.example.projectsigma

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.projectsigma.data.AuthRepository
import com.example.projectsigma.data.EventsRepository
import com.example.projectsigma.ui.auth.AuthScreen
import com.example.projectsigma.ui.main.MainScreen
import com.example.projectsigma.viewmodel.AuthViewModel
import com.example.projectsigma.viewmodel.MainViewModel

@Composable
fun App() {
    val authRepository = remember { AuthRepository() }
    val eventsRepository = remember { EventsRepository() }

    val authViewModel = remember { AuthViewModel(authRepository) }
    val mainViewModel = remember { MainViewModel(eventsRepository, authRepository) }

    val currentUser by authViewModel.currentUser.collectAsState()

    MaterialTheme {
        Surface {
            if (currentUser == null) {
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
