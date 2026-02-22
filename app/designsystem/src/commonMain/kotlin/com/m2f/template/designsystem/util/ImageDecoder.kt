package com.m2f.template.designsystem.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

/**
 * Decodes a ByteArray into an ImageBitmap for display in Compose.
 * Platform-specific implementations handle the actual decoding.
 *
 * @param bytes The raw image bytes (PNG, JPEG, etc.)
 * @return ImageBitmap if decoding succeeds, null otherwise
 */
@Composable
expect fun rememberDecodedImage(bytes: ByteArray?): ImageBitmap?
