package com.example.project.shared.contract

class ServerAiService : AiService {
    override suspend fun generateText(prompt: String): String {
        return "Server-side AI not implemented yet. Use Android client."
    }
}
