package com.example.projectsigma

import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun getEpochMillis(): Long = kotlin.system.getTimeMillis()

actual fun shareText(context: Any?, text: String) {
    // Platform share handler
}

actual fun getDeviceGoogleAccounts(context: Any?): List<GoogleAccountItem> {
    return emptyList()
}

actual fun openOfficialGoogleSignInWebPage(context: Any?) {
    // Platform web page launcher
}

actual fun launchNativeGoogleAccountPicker(context: Any?, onAccountPicked: (email: String, displayName: String) -> Unit) {
    // Platform account picker launcher
}
