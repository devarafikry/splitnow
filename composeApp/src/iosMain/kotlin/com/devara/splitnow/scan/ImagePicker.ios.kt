@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
package com.devara.splitnow.scan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import platform.UIKit.*
import platform.darwin.NSObject
import platform.Foundation.NSData
import platform.posix.memcpy
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.addressOf

private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    val result = ByteArray(size)
    if (size == 0) return result
    result.usePinned { pinned ->
        memcpy(pinned.addressOf(0), bytes, size.toULong())
    }
    return result
}

private fun topMostViewController(): UIViewController? {
    val window = UIApplication.sharedApplication.keyWindow ?: return null
    var top = window.rootViewController
    while (top?.presentedViewController != null) top = top.presentedViewController
    return top
}

@Composable
actual fun rememberCameraPicker(onResult: (ByteArray?) -> Unit): () -> Unit {
    val cb by rememberUpdatedState(onResult)
    return remember {
        {
            val picker = UIImagePickerController()
            picker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
            val delegate = ImagePickerDelegate { bytes ->
                cb(bytes)
            }
            picker.delegate = delegate
            // Retain delegate via objc associated reference workaround: stash in static map.
            DelegateRetainer.retain(picker, delegate)
            topMostViewController()?.presentViewController(picker, true, null)
        }
    }
}

@Composable
actual fun rememberGalleryPicker(onResult: (ByteArray?) -> Unit): () -> Unit {
    val cb by rememberUpdatedState(onResult)
    return remember {
        {
            val picker = UIImagePickerController()
            picker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
            val delegate = ImagePickerDelegate { bytes -> cb(bytes) }
            picker.delegate = delegate
            DelegateRetainer.retain(picker, delegate)
            topMostViewController()?.presentViewController(picker, true, null)
        }
    }
}

private object DelegateRetainer {
    private val held = mutableMapOf<UIImagePickerController, ImagePickerDelegate>()
    fun retain(controller: UIImagePickerController, delegate: ImagePickerDelegate) {
        held[controller] = delegate
        delegate.onDone = { controller.dismissViewControllerAnimated(true, null); held.remove(controller) }
    }
}

private class ImagePickerDelegate(
    private val handler: (ByteArray?) -> Unit,
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
    var onDone: (() -> Unit)? = null

    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>,
    ) {
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        val data = image?.let { UIImageJPEGRepresentation(it, 0.9) }
        handler(data?.toByteArray())
        onDone?.invoke()
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        handler(null)
        onDone?.invoke()
    }
}
