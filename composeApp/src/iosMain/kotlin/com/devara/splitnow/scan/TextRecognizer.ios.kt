package com.devara.splitnow.scan

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.autoreleasepool
import kotlinx.cinterop.memScoped
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIImage
import platform.Vision.VNImageRequestHandler
import platform.Vision.VNRecognizeTextRequest
import platform.Vision.VNRecognizedTextObservation
import platform.Vision.VNRequestTextRecognitionLevelAccurate
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual class TextRecognizer actual constructor() {
    actual suspend fun recognize(imageBytes: ByteArray): String = suspendCancellableCoroutine { cont ->
        autoreleasepool {
            val nsData = imageBytes.toNSData()
            val image = UIImage.imageWithData(nsData) ?: run { cont.resume(""); return@autoreleasepool }
            val cgImage = image.CGImage ?: run { cont.resume(""); return@autoreleasepool }

            val request = VNRecognizeTextRequest { _, _ -> }
            request.recognitionLevel = VNRequestTextRecognitionLevelAccurate
            request.usesLanguageCorrection = true
            request.recognitionLanguages = listOf("en-US", "id-ID", "fr-FR", "de-DE", "es-ES", "it-IT", "pt-BR")

            val handler = VNImageRequestHandler(cgImage, emptyMap<Any?, Any>())
            try {
                handler.performRequests(listOf(request), null)
            } catch (_: Throwable) {
                cont.resume("")
                return@autoreleasepool
            }

            val lines = linkedSetOf<String>()
            val observations = request.results?.filterIsInstance<VNRecognizedTextObservation>().orEmpty()
            for (obs in observations) {
                val top = obs.topCandidates(1u).firstOrNull() as? platform.Vision.VNRecognizedText
                val text = top?.string()?.trim().orEmpty()
                if (text.isNotEmpty()) lines.add(text)
            }
            cont.resume(lines.joinToString("\n"))
        }
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData = memScoped {
    val bytes = allocArrayOf(this@toNSData)
    NSData.create(bytes = bytes, length = this@toNSData.size.toULong())
}
