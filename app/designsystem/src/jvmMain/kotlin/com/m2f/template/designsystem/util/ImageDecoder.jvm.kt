package com.m2f.template.designsystem.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import java.io.ByteArrayInputStream

@Suppress("DEPRECATION")
@Composable
actual fun rememberDecodedImage(bytes: ByteArray?): ImageBitmap? = remember(bytes) {
    bytes?.let { data ->
        try {
            loadImageBitmap(ByteArrayInputStream(data))
        } catch (e: Exception) {
            null
        }
    }
}
