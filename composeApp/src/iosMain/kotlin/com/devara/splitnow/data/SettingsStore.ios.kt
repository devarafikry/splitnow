package com.devara.splitnow.data

import platform.Foundation.NSUserDefaults

actual class SettingsStore actual constructor() {
    private val ud get() = NSUserDefaults.standardUserDefaults

    actual fun getString(key: String, default: String?): String? =
        (ud.stringForKey(key) ?: default)

    actual fun putString(key: String, value: String?) {
        if (value == null) ud.removeObjectForKey(key) else ud.setObject(value, forKey = key)
    }

    actual fun getBoolean(key: String, default: Boolean): Boolean {
        if (ud.objectForKey(key) == null) return default
        return ud.boolForKey(key)
    }

    actual fun putBoolean(key: String, value: Boolean) {
        ud.setBool(value, forKey = key)
    }
}
