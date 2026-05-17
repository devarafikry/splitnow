package com.devara.splitnow.share

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.devara.splitnow.data.AndroidContextHolder
import java.io.File

actual class SharePngLauncher actual constructor() {

    actual fun shareImage(bytes: ByteArray, suggestedFileName: String) {
        val ctx = AndroidContextHolder.context as? Context ?: return
        val shareDir = File(ctx.cacheDir, "shared").apply { mkdirs() }
        val file = File(shareDir, suggestedFileName)
        file.writeBytes(bytes)
        val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Share split").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        ctx.startActivity(chooser)
    }
}
