package com.example.project.shared.contract

actual fun getApiKey(): String {
    return BuildConfig.API_KEY
}
