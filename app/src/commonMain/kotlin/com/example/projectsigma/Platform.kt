package com.example.projectsigma

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun getEpochMillis(): Long
