package com.example.projectsigma.auth

import android.accounts.AccountManager
import android.content.Intent

object GoogleSignInHelper {

    const val WEB_CLIENT_ID = "692599024856-ikoc5a4le4uub5aq76smifhb0nqfp3g3.apps.googleusercontent.com"

    fun createChooseAccountIntent(): Intent {
        return AccountManager.newChooseAccountIntent(
            null, null, arrayOf("com.google"), null, null, null, null
        )
    }
}

data class GoogleUserData(
    val idToken: String,
    val email: String,
    val displayName: String,
    val photoUrl: String? = null
)
