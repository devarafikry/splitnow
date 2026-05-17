package com.devara.splitnow.scan

expect class TextRecognizer() {
    suspend fun recognize(imageBytes: ByteArray): String
}
