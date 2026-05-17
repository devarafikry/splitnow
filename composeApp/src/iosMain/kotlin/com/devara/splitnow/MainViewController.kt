package com.devara.splitnow

import androidx.compose.ui.window.ComposeUIViewController
import com.devara.splitnow.di.initKoin

private var koinInitialized = false

@Suppress("FunctionName", "unused") // called from Swift
fun MainViewController() = ComposeUIViewController(
    configure = {
        if (!koinInitialized) {
            initKoin()
            koinInitialized = true
        }
    },
) {
    App()
}
