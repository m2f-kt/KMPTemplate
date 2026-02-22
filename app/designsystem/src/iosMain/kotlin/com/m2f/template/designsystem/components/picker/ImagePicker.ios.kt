package com.m2f.template.designsystem.components.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSItemProvider
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UniformTypeIdentifiers.UTTypeImage
import platform.darwin.NSObject
import platform.posix.memcpy

/**
 * iOS implementation of the image picker using PHPickerViewController.
 */
@Composable
actual fun rememberImagePickerLauncher(
    onImagePicked: (ImagePickerResult?) -> Unit,
): () -> Unit {
    val callback = remember { onImagePicked }

    return {
        val configuration = PHPickerConfiguration().apply {
            filter = PHPickerFilter.imagesFilter
            selectionLimit = 1
        }

        val picker = PHPickerViewController(configuration = configuration)
        val delegate = ImagePickerDelegate(callback)
        picker.delegate = delegate

        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
        rootViewController?.presentViewController(picker, animated = true, completion = null)
    }
}

private class ImagePickerDelegate(
    private val onImagePicked: (ImagePickerResult?) -> Unit,
) : NSObject(), PHPickerViewControllerDelegateProtocol {

    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        picker.dismissViewControllerAnimated(true, completion = null)

        @Suppress("UNCHECKED_CAST")
        val results = didFinishPicking as? List<PHPickerResult> ?: run {
            onImagePicked(null)
            return
        }

        if (results.isEmpty()) {
            onImagePicked(null)
            return
        }

        val result = results.first()
        val itemProvider: NSItemProvider = result.itemProvider

        // Use UTTypeImage to load image data
        if (itemProvider.hasItemConformingToTypeIdentifier(UTTypeImage.identifier)) {
            itemProvider.loadDataRepresentationForTypeIdentifier(
                typeIdentifier = UTTypeImage.identifier,
            ) { data, error ->
                if (error != null || data == null) {
                    onImagePicked(null)
                    return@loadDataRepresentationForTypeIdentifier
                }

                // Convert NSData to UIImage and then back to JPEG data for consistency
                val image = UIImage.imageWithData(data)
                if (image == null) {
                    onImagePicked(null)
                    return@loadDataRepresentationForTypeIdentifier
                }

                val jpegData = UIImageJPEGRepresentation(image, 0.85) ?: run {
                    onImagePicked(null)
                    return@loadDataRepresentationForTypeIdentifier
                }

                val bytes = jpegData.toByteArray()
                onImagePicked(ImagePickerResult(bytes, "avatar.jpg", "image/jpeg"))
            }
        } else {
            onImagePicked(null)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val length = this.length.toInt()
    val bytes = ByteArray(length)
    if (length > 0) {
        bytes.usePinned { pinned ->
            memcpy(pinned.addressOf(0), this.bytes, this.length)
        }
    }
    return bytes
}
