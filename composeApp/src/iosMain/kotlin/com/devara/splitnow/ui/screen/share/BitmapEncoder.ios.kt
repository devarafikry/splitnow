package com.devara.splitnow.ui.screen.share

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import platform.CoreFoundation.CFRelease
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGBitmapContextCreateImage
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGImageAlphaInfo
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIImage
import platform.UIKit.UIImagePNGRepresentation
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual suspend fun encodeBitmapToPng(image: ImageBitmap): ByteArray {
    val pixelMap = image.toPixelMap()
    val w = pixelMap.width
    val h = pixelMap.height
    if (w == 0 || h == 0) return ByteArray(0)

    val rgbaBytes = ByteArray(w * h * 4)
    for (y in 0 until h) {
        for (x in 0 until w) {
            val c = pixelMap[x, y]
            val idx = (y * w + x) * 4
            // Compose Color packs ARGB; export as premultiplied RGBA for CGBitmapContext.
            val a = (c.alpha * 255f).toInt().coerceIn(0, 255)
            val r = (c.red * 255f * c.alpha).toInt().coerceIn(0, 255)
            val g = (c.green * 255f * c.alpha).toInt().coerceIn(0, 255)
            val b = (c.blue * 255f * c.alpha).toInt().coerceIn(0, 255)
            rgbaBytes[idx] = r.toByte()
            rgbaBytes[idx + 1] = g.toByte()
            rgbaBytes[idx + 2] = b.toByte()
            rgbaBytes[idx + 3] = a.toByte()
        }
    }

    val pngBytes: ByteArray = memScoped {
        val colorSpace = CGColorSpaceCreateDeviceRGB()
        val pinned = rgbaBytes.usePinned { buf ->
            val ctx = CGBitmapContextCreate(
                buf.addressOf(0),
                w.toULong(),
                h.toULong(),
                8u,
                (w * 4).toULong(),
                colorSpace,
                CGImageAlphaInfo.kCGImageAlphaPremultipliedLast.value,
            ) ?: return@memScoped ByteArray(0)
            val cgImage = CGBitmapContextCreateImage(ctx) ?: return@memScoped ByteArray(0)
            val uiImage = UIImage.imageWithCGImage(cgImage)
            val data: NSData? = UIImagePNGRepresentation(uiImage)
            val size = data?.length?.toInt() ?: 0
            val out = ByteArray(size)
            if (size > 0) {
                out.usePinned { p -> memcpy(p.addressOf(0), data!!.bytes, size.toULong()) }
            }
            CFRelease(cgImage)
            CFRelease(ctx)
            out
        }
        CFRelease(colorSpace)
        pinned
    }
    return pngBytes
}
