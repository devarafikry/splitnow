package com.devara.splitnow.platform

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.devara.splitnow.data.AndroidContextHolder

actual class UrlOpener actual constructor() {
    actual fun open(url: String) {
        val ctx = AndroidContextHolder.context as? Context ?: return
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { ctx.startActivity(intent) }
    }
}
