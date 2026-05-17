package com.devara.splitnow.di

import platform.Foundation.NSBundle

actual fun geminiApiKey(): String =
    NSBundle.mainBundle.objectForInfoDictionaryKey("SPLITNOW_GEMINI_KEY") as? String ?: ""
