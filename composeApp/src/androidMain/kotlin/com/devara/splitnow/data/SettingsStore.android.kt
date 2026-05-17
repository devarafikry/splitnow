package com.devara.splitnow.data

import android.content.Context

actual class SettingsStore actual constructor() {
    private val prefs by lazy {
        val ctx = AndroidContextHolder.context as? Context
            ?: error("AndroidContextHolder.context not set")
        ctx.getSharedPreferences("splitnow", Context.MODE_PRIVATE)
    }

    actual fun getString(key: String, default: String?): String? =
        prefs.getString(key, default)

    actual fun putString(key: String, value: String?) {
        prefs.edit().also { ed -> if (value == null) ed.remove(key) else ed.putString(key, value) }.apply()
    }

    actual fun getBoolean(key: String, default: Boolean): Boolean =
        prefs.getBoolean(key, default)

    actual fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }
}
