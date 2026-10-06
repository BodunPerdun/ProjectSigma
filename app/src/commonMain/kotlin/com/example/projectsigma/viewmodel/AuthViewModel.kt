package com.example.projectsigma.viewmodel

import com.example.projectsigma.data.AuthRepository
import com.example.projectsigma.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) {

    private val scope = CoroutineScope(Dispatchers.Main)

    val currentUser: StateFlow<User?> = authRepository.currentUser

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _displayName = MutableStateFlow("")
    val displayName: StateFlow<String> = _displayName.asStateFlow()

    private val _isRegisterMode = MutableStateFlow(false)
    val isRegisterMode: StateFlow<Boolean> = _isRegisterMode.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun onEmailChanged(value: String) { _email.value = value }
    fun onPasswordChanged(value: String) { _password.value = value }
    fun onDisplayNameChanged(value: String) { _displayName.value = value }

    fun toggleAuthMode() {
        _isRegisterMode.value = !_isRegisterMode.value
        _errorMessage.value = null
    }

    fun submit() {
        _errorMessage.value = null
        _isLoading.value = true

        scope.launch {
            val result = if (_isRegisterMode.value) {
                authRepository.registerWithEmail(_email.value, _password.value, _displayName.value)
            } else {
                authRepository.loginWithEmail(_email.value, _password.value)
            }

            _isLoading.value = false
            result.onFailure {
                _errorMessage.value = it.message ?: "Authentication failed."
            }
        }
    }

    fun signInWithGoogle() {
        _errorMessage.value = null
        _isLoading.value = true

        scope.launch {
            val result = authRepository.signInWithGoogle()
            _isLoading.value = false
            result.onFailure {
                _errorMessage.value = it.message ?: "Google Sign-In failed."
            }
        }
    }

    fun logout() {
        authRepository.logout()
    }

    fun updateLocationVisibility(isVisible: Boolean) {
        authRepository.updateLocationVisibility(isVisible)
    }

    fun updateSocialsPublicity(isPublic: Boolean) {
        authRepository.updateSocialsPublicity(isPublic)
    }

    fun updateProfilePhoto(photoUrl: String?) {
        authRepository.updateProfilePhoto(photoUrl)
    }

    fun updateSocialHandles(instagram: String?, telegram: String?) {
        authRepository.updateSocialHandles(instagram, telegram)
    }

    fun addFriend(friendId: String) {
        authRepository.addFriend(friendId)
    }

    fun removeFriend(friendId: String) {
        authRepository.removeFriend(friendId)
    }

    fun getDiscoverableNearbyUsers(): List<Pair<User, String>> {
        return authRepository.getDiscoverableNearbyUsers()
    }

    fun getFriendUsers(): List<User> {
        val friendIds = currentUser.value?.friends ?: emptyList()
        return authRepository.getDiscoverableNearbyUsers()
            .map { it.first }
            .filter { friendIds.contains(it.id) }
    }
}
