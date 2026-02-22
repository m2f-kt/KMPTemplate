package com.m2f.template.designsystem.util

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

@Composable
actual fun rememberDecodedImage(bytes: ByteArray?): ImageBitmap? = remember(bytes) {
    bytes?.let { data ->
        try {
            BitmapFactory.decodeByteArray(data, 0, data.size)?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
}
