package com.devara.splitnow.share

expect class SharePngLauncher() {
    fun shareImage(bytes: ByteArray, suggestedFileName: String = "splitnow-receipt.png")
}
