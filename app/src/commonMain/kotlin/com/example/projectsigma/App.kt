package com.example.projectsigma

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.projectsigma.data.AuthRepository
import com.example.projectsigma.data.EventsRepository
import com.example.projectsigma.data.FriendRequestService
import com.example.projectsigma.data.NotificationsRepository
import com.example.projectsigma.theme.LocaPopTheme
import com.example.projectsigma.ui.auth.AuthScreen
import com.example.projectsigma.ui.main.MainScreen
import com.example.projectsigma.ui.splash.SplashScreen
import com.example.projectsigma.viewmodel.AuthViewModel
import com.example.projectsigma.viewmodel.MainViewModel

@Composable
fun App(
    eventsRepository: EventsRepository,
    authRepository: AuthRepository,
    notificationsRepository: NotificationsRepository,
    friendRequestService: FriendRequestService
) {
    var isSplashVisible by remember { mutableStateOf(true) }

    val authViewModel = remember(authRepository) { AuthViewModel(authRepository) }
    val mainViewModel = remember(eventsRepository, authRepository) { MainViewModel(eventsRepository, authRepository) }

    val currentUser by authViewModel.currentUser.collectAsState()

    LocaPopTheme {
        Surface {
            if (isSplashVisible) {
                SplashScreen(onSplashFinished = { isSplashVisible = false })
            } else if (currentUser == null) {
                AuthScreen(authViewModel = authViewModel)
            } else {
                MainScreen(
                    mainViewModel = mainViewModel,
                    authViewModel = authViewModel,
                    notificationsRepository = notificationsRepository,
                    friendRequestService = friendRequestService
                )
            }
        }
    }
}
