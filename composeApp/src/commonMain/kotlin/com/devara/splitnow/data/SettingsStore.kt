package com.devara.splitnow.data

/** Lightweight key-value preference store. expect/actual; platform-backed. */
expect class SettingsStore() {
    fun getString(key: String, default: String? = null): String?
    fun putString(key: String, value: String?)
    fun getBoolean(key: String, default: Boolean = false): Boolean
    fun putBoolean(key: String, value: Boolean)
}

const val PREF_DEFAULT_CURRENCY = "default_currency"
const val PREF_THEME = "theme" // "system" | "light" | "dark"
const val PREF_LOCALE = "locale" // e.g. "en", "id", "ja", "zh-Hans"
const val PREF_ONBOARDED = "onboarded"
const val PREF_GEMINI_KEY_OVERRIDE = "gemini_key_override"
