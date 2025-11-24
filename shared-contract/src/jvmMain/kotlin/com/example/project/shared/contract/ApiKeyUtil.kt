package com.example.project.shared.contract

actual fun getApiKey(): String {
    return System.getenv("API_KEY") ?: ""
}
