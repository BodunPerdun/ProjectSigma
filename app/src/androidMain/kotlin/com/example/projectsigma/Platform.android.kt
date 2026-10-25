package com.example.projectsigma

import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import com.example.projectsigma.auth.GoogleOAuthAppAuthHelper
import com.example.projectsigma.auth.GoogleSignInHelper

var currentActivityContext: Context? = null
var globalAccountPickerLauncher: ((Intent) -> Unit)? = null

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.RELEASE}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun getEpochMillis(): Long = System.currentTimeMillis()

actual fun shareText(context: Any?, text: String) {
    val ctx = (context as? Context) ?: currentActivityContext ?: return
    runCatching {
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
    }.onFailure { e ->
        Log.e("Platform", "Failed to launch share chooser: ${e.message}")
    }
}

actual fun getDeviceGoogleAccounts(context: Any?): List<GoogleAccountItem> {
    val ctx = (context as? Context) ?: currentActivityContext ?: return emptyList()
    return runCatching {
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
    }.getOrDefault(emptyList())
}

actual fun openOfficialGoogleSignInWebPage(context: Any?) {
    val ctx = (context as? Context) ?: currentActivityContext ?: return
    runCatching {
        val intent = GoogleOAuthAppAuthHelper.createAuthIntent(ctx)
        val launcher = globalAccountPickerLauncher
        if (launcher != null) {
            launcher.invoke(intent)
        } else {
            if (ctx !is android.app.Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ctx.startActivity(intent)
        }
    }.onFailure { e ->
        Log.e("GoogleSignIn", "Failed to launch AppAuth Google sign-in: ${e.message}")
    }
}

actual fun launchNativeGoogleAccountPicker(context: Any?, onAccountPicked: (email: String, displayName: String) -> Unit) {
    val ctx = (context as? Context) ?: currentActivityContext ?: return
    runCatching {
        val intent = GoogleOAuthAppAuthHelper.createAuthIntent(ctx)
        val launcher = globalAccountPickerLauncher
        if (launcher != null) {
            launcher.invoke(intent)
        } else {
            if (ctx !is android.app.Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ctx.startActivity(intent)
        }
    }.onFailure { e ->
        Log.e("GoogleSignIn", "Failed to launch AppAuth account picker: ${e.message}")
    }
}
