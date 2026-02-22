package com.m2f.template.designsystem.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.create

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
@Composable
actual fun rememberDecodedImage(bytes: ByteArray?): ImageBitmap? = remember(bytes) {
    bytes?.let { data ->
        try {
            // Convert ByteArray to NSData then decode with Skia
            val nsData = data.usePinned { pinned ->
                NSData.create(bytes = pinned.addressOf(0), length = data.size.toULong())
            }
            Image.makeFromEncoded(nsData.toByteArray()).toComposeImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = this.length.toInt()
    return ByteArray(size).apply {
        usePinned { pinned ->
            platform.posix.memcpy(pinned.addressOf(0), this@toByteArray.bytes, this@toByteArray.length)
        }
    }
}
