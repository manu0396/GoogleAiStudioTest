package com.example.project.shared.contract

import kotlinx.coroutines.flow.Flow

interface AiService {
    suspend fun generateContent(prompt: String): String
    fun generateContentStream(prompt: String): Flow<String>
}
