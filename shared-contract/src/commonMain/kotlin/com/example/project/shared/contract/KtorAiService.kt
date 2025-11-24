package com.example.project.shared.contract

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

// --- DTOs ---
@Serializable
data class GeminiRequest(
    val contents: List<Content>,
    @SerialName("system_instruction")
    val systemInstruction: Content? = null
)

@Serializable
data class Content(val parts: List<Part>, val role: String? = "user")

@Serializable
data class Part(val text: String)

@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>? = null,
    val error: ApiError? = null
)

@Serializable
data class ApiError(val code: Int? = null, val message: String? = null)

@Serializable
data class Candidate(val content: Content)

// --- Service ---
class KtorAiService(private val apiKey: String) : AiService {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                explicitNulls = false // Important for partial responses
            })
        }
    }

    private val systemPrompt = """
        You are a Senior Android Developer & Architect.
        Your expertise is strictly focused on:
        1. Kotlin 2.0 + Coroutines + Flow.
        2. Jetpack Compose (Modern UI).
        3. Kotlin Multiplatform (KMP) architecture.
        
        Rules:
        - Provide concise, production-ready code snippets.
        - Always prefer 'libs.versions.toml' for dependencies.
        - Criticize usage of XML or Java (suggest Compose/Kotlin instead).
        - If asked about architecture, default to Clean Architecture (Data -> Domain -> UI).
    """.trimIndent()

    override suspend fun generateText(prompt: String): String {
        return try {
            // FIX: Use the exact model string that works for REST
            // Sometimes 'gemini-1.5-flash-latest' is safer if the specific version is rolling out
            val model = "gemini-1.5-flash"

            val response: GeminiResponse = client.post("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent") {
                parameter("key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(
                    GeminiRequest(
                        contents = listOf(Content(listOf(Part(prompt)))),
                        systemInstruction = Content(listOf(Part(systemPrompt)))
                    )
                )
            }.body()

            if (response.error != null) {
                // If 404 persists, it means the API key region or Model ID is wrong.
                return "API Error (${response.error.code}): ${response.error.message}"
            }

            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "No response from Gemini."
        } catch (e: Exception) {
            "Network Error: ${e.message}"
        }
    }
}
