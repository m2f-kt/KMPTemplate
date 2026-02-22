package com.m2f.template.designsystem.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image

@Composable
actual fun rememberDecodedImage(bytes: ByteArray?): ImageBitmap? = remember(bytes) {
    bytes?.let { data ->
        try {
            Image.makeFromEncoded(data).toComposeImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
}
