package com.devara.splitnow.l10n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.devara.splitnow.ai.GeminiClient
import com.devara.splitnow.data.PREF_LOCALE
import com.devara.splitnow.data.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * Runtime translation service. Holds a (locale → English → translated) cache.
 *
 *  - On startup, reads the persisted locale and any cached translations from
 *    SettingsStore (one JSON blob per locale).
 *  - When the user picks a new locale, batch-translates the entire English
 *    catalogue via one Gemini call, caches the result, and notifies observers.
 *  - `@Composable tr(en)` reads from the in-memory map; returns English
 *    fallback if a translation for that exact source string is missing.
 */
class Translator(
    private val client: GeminiClient,
    private val settings: SettingsStore,
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val _locale = MutableStateFlow(settings.getString(PREF_LOCALE, "en") ?: "en")
    val locale: StateFlow<String> = _locale.asStateFlow()

    private val _translations = MutableStateFlow<Map<String, String>>(loadCached(_locale.value))
    val translations: StateFlow<Map<String, String>> = _translations.asStateFlow()

    fun setLocale(code: String) {
        if (code == _locale.value) return
        _locale.value = code
        // Try cache first.
        val cached = loadCached(code)
        if (cached.isNotEmpty() || code == "en") {
            _translations.value = cached
            return
        }
        // Otherwise translate via Gemini.
        scope.launch { translateAll(code) }
    }

    private fun loadCached(code: String): Map<String, String> {
        if (code == "en") return emptyMap()
        val raw = settings.getString(translationKey(code)) ?: return emptyMap()
        return runCatching {
            val obj = json.parseToJsonElement(raw) as JsonObject
            obj.entries.associate { (k, v) -> k to v.jsonPrimitive.content }
        }.getOrDefault(emptyMap())
    }

    private suspend fun translateAll(target: String) {
        val locale = Locales.all.firstOrNull { it.code == target } ?: return
        val payload = buildJsonObject {
            Strings.all.distinct().forEachIndexed { i, src ->
                put(i.toString(), JsonPrimitive(src))
            }
        }.toString()
        val prompt = """
You are a localization assistant for a split-bill mobile app called SplitNow.

Translate every value in this JSON object from English to ${locale.englishName} (locale code ${locale.code}).
Return a JSON object with the SAME keys and the translated values. Output JSON ONLY — no prose, no fences.

Style:
- Casual, friendly, app-UI register.
- Keep length comparable to source (button labels short).
- Keep punctuation, line breaks, and currency placeholders intact.
- "AI" stays "AI". Brand "SplitNow" stays untranslated.

JSON to translate:
$payload
        """.trimIndent()

        runCatching {
            val raw = client.generate(prompt).trim()
            val cleaned = stripCodeFences(raw)
            val obj = json.parseToJsonElement(cleaned) as JsonObject
            val byKey: Map<String, String> = obj.entries.associate { (k, v) -> k to v.jsonPrimitive.content }
            // Map back to (English → translated) using the same Strings.all order.
            val translations: Map<String, String> = Strings.all.distinct().mapIndexedNotNull { i, src ->
                val t = byKey[i.toString()] ?: return@mapIndexedNotNull null
                src to t
            }.toMap()
            // Cache
            settings.putString(translationKey(target), buildJsonObject {
                translations.forEach { (k, v) -> put(k, JsonPrimitive(v)) }
            }.toString())
            _translations.value = translations
        }.onFailure {
            println("Translator: failed to translate to $target: ${it.message}")
        }
    }

    private fun stripCodeFences(raw: String): String {
        var s = raw
        if (s.startsWith("```")) {
            s = s.removePrefix("```json").removePrefix("```").trim()
            if (s.endsWith("```")) s = s.removeSuffix("```").trim()
        }
        return s
    }

    private fun translationKey(code: String) = "translations.$code"
}

/**
 * Composable helper. Returns the translated text or the English source as fallback.
 * Wrap user-facing literals as `Text(tr(Strings.SOME_KEY))` to opt them into
 * runtime localization.
 */
@Composable
fun tr(en: String): String {
    val translator = org.koin.compose.koinInject<Translator>()
    val map by translator.translations.collectAsState()
    return map[en] ?: en
}
