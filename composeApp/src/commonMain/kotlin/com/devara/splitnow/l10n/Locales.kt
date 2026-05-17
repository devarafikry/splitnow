package com.devara.splitnow.l10n

data class LocaleChoice(
    val code: String,
    /** Native-script label shown in the picker (English, Bahasa Indonesia, 日本語). */
    val label: String,
    /** English-friendly name for sub-row. */
    val englishName: String,
)

/**
 * Languages supported by iOS, presented in their native script. SplitNow
 * persists the picked code in `PREF_LOCALE`; the Translator service then
 * batch-translates the English string catalogue into that locale via Gemini
 * and caches the result.
 */
object Locales {
    val all: List<LocaleChoice> = listOf(
        LocaleChoice("en", "English", "English"),
        LocaleChoice("id", "Bahasa Indonesia", "Indonesian"),
        LocaleChoice("ms", "Bahasa Melayu", "Malay"),
        LocaleChoice("zh-Hans", "简体中文", "Chinese (Simplified)"),
        LocaleChoice("zh-Hant", "繁體中文", "Chinese (Traditional)"),
        LocaleChoice("ja", "日本語", "Japanese"),
        LocaleChoice("ko", "한국어", "Korean"),
        LocaleChoice("th", "ไทย", "Thai"),
        LocaleChoice("vi", "Tiếng Việt", "Vietnamese"),
        LocaleChoice("hi", "हिन्दी", "Hindi"),
        LocaleChoice("ar", "العربية", "Arabic"),
        LocaleChoice("he", "עברית", "Hebrew"),
        LocaleChoice("fa", "فارسی", "Persian"),
        LocaleChoice("tr", "Türkçe", "Turkish"),
        LocaleChoice("ru", "Русский", "Russian"),
        LocaleChoice("uk", "Українська", "Ukrainian"),
        LocaleChoice("pl", "Polski", "Polish"),
        LocaleChoice("de", "Deutsch", "German"),
        LocaleChoice("fr", "Français", "French"),
        LocaleChoice("es", "Español", "Spanish"),
        LocaleChoice("pt-BR", "Português (BR)", "Portuguese (Brazil)"),
        LocaleChoice("pt-PT", "Português (PT)", "Portuguese (Portugal)"),
        LocaleChoice("it", "Italiano", "Italian"),
        LocaleChoice("nl", "Nederlands", "Dutch"),
        LocaleChoice("sv", "Svenska", "Swedish"),
        LocaleChoice("nb", "Norsk bokmål", "Norwegian Bokmål"),
        LocaleChoice("da", "Dansk", "Danish"),
        LocaleChoice("fi", "Suomi", "Finnish"),
        LocaleChoice("cs", "Čeština", "Czech"),
        LocaleChoice("sk", "Slovenčina", "Slovak"),
        LocaleChoice("hu", "Magyar", "Hungarian"),
        LocaleChoice("ro", "Română", "Romanian"),
        LocaleChoice("el", "Ελληνικά", "Greek"),
        LocaleChoice("hr", "Hrvatski", "Croatian"),
        LocaleChoice("ca", "Català", "Catalan"),
    )
}

enum class ThemeChoice(val id: String, val label: String, val description: String) {
    SYSTEM("system", "System", "Match the device setting"),
    LIGHT("light", "Light", "Always use light mode"),
    DARK("dark", "Dark", "Always use dark mode"),
}
