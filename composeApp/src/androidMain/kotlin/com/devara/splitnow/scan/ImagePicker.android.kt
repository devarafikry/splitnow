package com.devara.splitnow.scan

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import java.io.ByteArrayOutputStream

@Composable
actual fun rememberCameraPicker(onResult: (ByteArray?) -> Unit): () -> Unit {
    val cb by rememberUpdatedState(onResult)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp: Bitmap? ->
        if (bmp == null) { cb(null); return@rememberLauncherForActivityResult }
        val baos = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 90, baos)
        cb(baos.toByteArray())
    }
    return { launcher.launch(null) }
}

@Composable
actual fun rememberGalleryPicker(onResult: (ByteArray?) -> Unit): () -> Unit {
    val cb by rememberUpdatedState(onResult)
    val ctx = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) { cb(null); return@rememberLauncherForActivityResult }
        runCatching {
            ctx.contentResolver.openInputStream(uri)?.use { input ->
                cb(input.readBytes())
            } ?: cb(null)
        }.onFailure { cb(null) }
    }
    return { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
}
