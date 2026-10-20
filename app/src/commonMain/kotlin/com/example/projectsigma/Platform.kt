package com.example.projectsigma

import kotlinx.serialization.Serializable

interface Platform {
    val name: String
}

@Serializable
data class GoogleAccountItem(
    val email: String,
    val displayName: String,
    val avatarUrl: String
)

expect fun getPlatform(): Platform

expect fun getEpochMillis(): Long

expect fun shareText(context: Any?, text: String)

expect fun getDeviceGoogleAccounts(context: Any?): List<GoogleAccountItem>

expect fun openOfficialGoogleSignInWebPage(context: Any?)

expect fun launchNativeGoogleAccountPicker(context: Any?, onAccountPicked: (email: String, displayName: String) -> Unit)
