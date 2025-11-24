package com.example.project.shared.contract

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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

// --- DTOs for Model Listing ---
@Serializable
data class ModelListResponse(val models: List<ModelInfo>? = null)

@Serializable
data class ModelInfo(val name: String)

// --- Service ---
class KtorAiService(
    private val apiKey: String,
    private val client: HttpClient // Injected via DI (Koin/Hilt)
) : AiService {

    // 1. UPDATED PERSONA: Senior Android Developer
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

    override suspend fun generateContent(prompt: String): String {
        return try {
            // 1. Dynamic Discovery: Ask Google what models I can use
            // This avoids the 404 error by finding the valid model string for your region/key
            val modelsUrl = "https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey"
            val listResponse: ModelListResponse = client.get(modelsUrl).body()

            // 2. Select Best Model: Prefer Flash, fallback to first available
            val validModelName = listResponse.models
                ?.map { it.name }
                ?.firstOrNull { it.contains("flash", ignoreCase = true) }
                ?: listResponse.models?.firstOrNull()?.name
                ?: throw Exception("No available models found for this API key.")

            // 3. Generate Content
            // Note: validModelName already contains "models/" prefix from the API list
            val generateUrl = "https://generativelanguage.googleapis.com/v1beta/$validModelName:generateContent"

            val response = client.post(generateUrl) {
                parameter("key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(
                    GeminiRequest(
                        contents = listOf(Content(listOf(Part(prompt)))),
                        systemInstruction = Content(listOf(Part(systemPrompt)))
                    )
                )
            }

            val responseBody = response.body<GeminiResponse>()

            if (responseBody.error != null) {
                return "API Error (${responseBody.error.code}): ${responseBody.error.message}"
            }

            responseBody.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "No response from Gemini."

        } catch (e: Exception) {
            "Network Error: ${e.message}"
        }
    }

    override fun generateContentStream(prompt: String): Flow<String> = flow {
        try {
            // Identical discovery logic for streaming (simplified for brevity)
            // Ideally, cache 'validModelName' in a variable so you don't fetch it every time
            val modelsUrl = "https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey"
            val listResponse: ModelListResponse = client.get(modelsUrl).body()
            val validModelName = listResponse.models
                ?.map { it.name }
                ?.firstOrNull { it.contains("flash", ignoreCase = true) }
                ?: listResponse.models?.firstOrNull()?.name
                ?: "models/gemini-1.5-flash" // Fallback

            val url = "https://generativelanguage.googleapis.com/v1beta/$validModelName:streamGenerateContent?key=$apiKey"

            client.preparePost(url) {
                contentType(ContentType.Application.Json)
                setBody(
                    GeminiRequest(
                        contents = listOf(Content(listOf(Part(prompt)))),
                        systemInstruction = Content(listOf(Part(systemPrompt)))
                    )
                )
            }.execute { httpResponse ->
                val channel: ByteReadChannel = httpResponse.bodyAsChannel()
                while (!channel.isClosedForRead) {
                    val line = channel.readUTF8Line() ?: continue
                    if (line.startsWith("data:")) {
                        val jsonStr = line.removePrefix("data:").trim()
                        if (jsonStr == "[DONE]") break

                        try {
                            val chunk = Json { ignoreUnknownKeys = true }.decodeFromString<GeminiResponse>(jsonStr)
                            val text = chunk.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                            if (!text.isNullOrEmpty()) {
                                emit(text)
                            }
                        } catch (e: Exception) {
                            // Ignore parse errors for intermediate chunks
                        }
                    }
                }
            }
        } catch (e: Exception) {
            emit("Stream Error: ${e.message}")
        }
    }
}
