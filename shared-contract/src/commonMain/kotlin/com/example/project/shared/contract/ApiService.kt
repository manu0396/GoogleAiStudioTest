package com.example.project.shared.contract

interface AiService {
    suspend fun generateText(prompt: String): String
}
