package com.example.projectsigma

import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.example.projectsigma.auth.GoogleSignInHelper

var currentActivityContext: Context? = null
var globalAccountPickerLauncher: ((Intent) -> Unit)? = null

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.RELEASE}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun getEpochMillis(): Long = System.currentTimeMillis()

actual fun shareText(context: Any?, text: String) {
    val ctx = (context as? Context) ?: currentActivityContext
    if (ctx != null) {
        try {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Share Event via").apply {
                if (ctx !is android.app.Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            ctx.startActivity(shareIntent)
        } catch (e: Exception) {
            android.util.Log.e("Platform", "Error launching share chooser: ${e.message}")
        }
    }
}

actual fun getDeviceGoogleAccounts(context: Any?): List<GoogleAccountItem> {
    val ctx = (context as? Context) ?: currentActivityContext ?: return emptyList()
    return try {
        val manager = AccountManager.get(ctx)
        val googleAccounts = manager.getAccountsByType("com.google")
        googleAccounts.map { acc ->
            val email = acc.name
            val name = email.substringBefore("@")
                .replace(".", " ")
                .replace("_", " ")
                .split(" ")
                .joinToString(" ") { word -> word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }

            GoogleAccountItem(
                email = email,
                displayName = name,
                avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150"
            )
        }
    } catch (e: Exception) {
        emptyList()
    }
}

actual fun openOfficialGoogleSignInWebPage(context: Any?) {
    val ctx = (context as? Context) ?: currentActivityContext ?: return
    try {
        val intent = AccountManager.newChooseAccountIntent(
            null, null, arrayOf("com.google"), null, null, null, null
        ).apply {
            if (ctx !is android.app.Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        ctx.startActivity(intent)
    } catch (e: Exception) {
        try {
            val googleOAuthUrl = "https://accounts.google.com/o/oauth2/v2/auth" +
                    "?client_id=${GoogleSignInHelper.WEB_CLIENT_ID}" +
                    "&redirect_uri=urn:ietf:wg:oauth:2.0:oob" +
                    "&response_type=token%20id_token" +
                    "&scope=openid%20profile%20email"

            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(googleOAuthUrl)).apply {
                if (ctx !is android.app.Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            ctx.startActivity(webIntent)
        } catch (ex: Exception) {
            android.util.Log.e("GoogleSignIn", "Error launching Google sign-in: ${ex.message}")
        }
    }
}

actual fun launchNativeGoogleAccountPicker(context: Any?, onAccountPicked: (email: String, displayName: String) -> Unit) {
    val ctx = (context as? Context) ?: currentActivityContext ?: return
    try {
        val intent = AccountManager.newChooseAccountIntent(
            null, null, arrayOf("com.google"), null, null, null, null
        )
        val launcher = globalAccountPickerLauncher
        if (launcher != null) {
            launcher.invoke(intent)
        } else {
            if (ctx !is android.app.Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ctx.startActivity(intent)
        }
    } catch (e: Exception) {
        android.util.Log.e("GoogleSignIn", "Error launching Google Account Chooser: ${e.message}")
    }
}
