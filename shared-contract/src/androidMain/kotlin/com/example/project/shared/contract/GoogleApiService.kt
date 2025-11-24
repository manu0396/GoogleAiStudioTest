package com.example.project.shared.contract

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig

class GoogleAiService(
    private val apiKey: String
) : AiService {

    // Initialize the model as you would in the "Starter Template"
    private val model = GenerativeModel(
        modelName = "gemini-3.0-pro", // Or "gemini-2.5-flash" if available to your key
        apiKey = apiKey,
        generationConfig = generationConfig {
            temperature = 0.7f
            topK = 30
            topP = 0.8f
            // These values match the defaults in AI Studio
        }
    )

    override suspend fun generateText(prompt: String): String {
        println("DEBUG: Requesting Gemini 1.5 Flash with key: ${apiKey.take(4)}...")
        return try {
            val response = model.generateContent(prompt)
            response.text ?: "No response generated."
        } catch (e: Exception) {
            "Error: ${e.localizedMessage}"
        }
    }
}
