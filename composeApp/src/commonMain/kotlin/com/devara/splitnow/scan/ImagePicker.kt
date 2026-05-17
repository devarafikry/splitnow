package com.devara.splitnow.scan

import androidx.compose.runtime.Composable

/**
 * Returns either captured image bytes (JPEG/PNG-encoded) or null if the user cancelled.
 * Concrete implementation per platform:
 *  - Android: ActivityResultContracts.TakePicturePreview (returns a Bitmap → JPEG bytes).
 *  - iOS: UIImagePickerController with .camera source type.
 */
@Composable
expect fun rememberCameraPicker(onResult: (ByteArray?) -> Unit): () -> Unit

@Composable
expect fun rememberGalleryPicker(onResult: (ByteArray?) -> Unit): () -> Unit
