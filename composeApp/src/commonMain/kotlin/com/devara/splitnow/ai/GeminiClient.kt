package com.devara.splitnow.ai

import com.devara.splitnow.data.PREF_GEMINI_KEY_OVERRIDE
import com.devara.splitnow.data.SettingsStore
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
internal data class GeminiContent(val parts: List<GeminiPart>)

@Serializable
internal data class GeminiPart(val text: String)

@Serializable
internal data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig,
)

@Serializable
internal data class GeminiGenerationConfig(
    val responseMimeType: String? = null,
    val temperature: Double = 0.2,
)

@Serializable
internal data class GeminiResponse(val candidates: List<GeminiCandidate> = emptyList())

@Serializable
internal data class GeminiCandidate(val content: GeminiContent)

class GeminiClient(
    private val settings: SettingsStore,
    private val fallbackKey: String,
) {
    private val modelId = "gemini-2.5-flash"
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }
    private val http = HttpClient {
        install(ContentNegotiation) { json(this@GeminiClient.json) }
        install(HttpTimeout) {
            requestTimeoutMillis = 60_000
            socketTimeoutMillis = 60_000
            connectTimeoutMillis = 20_000
        }
    }

    private fun resolveKey(): String =
        settings.getString(PREF_GEMINI_KEY_OVERRIDE)?.trim()?.takeIf { it.isNotEmpty() } ?: fallbackKey

    suspend fun generate(prompt: String): String {
        val key = resolveKey()
        if (key.isBlank()) error("Gemini API key missing. Set splitnow.gemini.key in ~/.gradle/gradle.properties or paste one in Settings.")

        val url = "https://generativelanguage.googleapis.com/v1/models/$modelId:generateContent?key=$key"
        var useMime = true
        var lastStatus = 0
        var lastBody = ""
        for (attempt in 0..1) {
            val response = http.post(url) {
                contentType(ContentType.Application.Json)
                setBody(GeminiRequest(
                    contents = listOf(GeminiContent(parts = listOf(GeminiPart(prompt)))),
                    generationConfig = GeminiGenerationConfig(
                        responseMimeType = if (useMime) "application/json" else null,
                    ),
                ))
            }
            if (response.status.isSuccess()) {
                val body: GeminiResponse = response.body()
                return body.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text.orEmpty()
            }
            lastStatus = response.status.value
            lastBody = response.bodyAsText().take(500)
            if (useMime && lastBody.contains("responseMimeType", ignoreCase = true) && attempt == 0) {
                useMime = false
                continue
            }
            break
        }
        println("SplitNow AI error $lastStatus: $lastBody")
        val friendly = when (lastStatus) {
            400 -> "AI couldn't read this receipt — try a clearer photo or shorter description."
            401, 403 -> "AI key was rejected. Open Settings → AI key and re-paste it."
            429 -> "Too many AI requests right now. Try again in a minute."
            in 500..599 -> "AI service is having a moment. Try again shortly."
            else -> "AI couldn't process this split right now."
        }
        error(friendly)
    }
}
