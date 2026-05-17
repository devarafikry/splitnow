package com.devara.splitnow.share

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.memScoped
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual class SharePngLauncher actual constructor() {
    actual fun shareImage(bytes: ByteArray, suggestedFileName: String) {
        val data: NSData = memScoped {
            val cBytes = allocArrayOf(bytes)
            NSData.create(bytes = cBytes, length = bytes.size.toULong())
        }
        val image = UIImage.imageWithData(data) ?: return
        val vc = UIActivityViewController(activityItems = listOf(image), applicationActivities = null)
        val window = UIApplication.sharedApplication.keyWindow
        var top = window?.rootViewController
        while (top?.presentedViewController != null) top = top.presentedViewController
        top?.presentViewController(vc, true, null)
    }
}
