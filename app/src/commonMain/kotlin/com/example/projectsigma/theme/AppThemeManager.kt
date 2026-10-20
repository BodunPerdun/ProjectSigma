package com.example.projectsigma.theme

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppThemeManager {
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private var persistenceHandler: ((Boolean) -> Unit)? = null

    val mapStyleUrl: String
        get() = "https://basemaps.cartocdn.com/gl/positron-gl-style/style.json"

    fun initPersistence(savedDarkState: Boolean?, handler: (Boolean) -> Unit) {
        persistenceHandler = handler
        if (savedDarkState != null) {
            _isDarkTheme.value = savedDarkState
        }
    }

    fun setDarkTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
        persistenceHandler?.invoke(isDark)
    }
}
