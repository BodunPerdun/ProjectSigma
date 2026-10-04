package com.example.projectsigma

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.RELEASE}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun getEpochMillis(): Long = System.currentTimeMillis()
