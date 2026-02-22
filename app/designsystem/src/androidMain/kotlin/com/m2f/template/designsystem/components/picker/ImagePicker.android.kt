package com.m2f.template.designsystem.components.picker

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Android implementation of the image picker using Photo Picker API.
 */
@Composable
actual fun rememberImagePickerLauncher(
    onImagePicked: (ImagePickerResult?) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        if (uri == null) {
            onImagePicked(null)
            return@rememberLauncherForActivityResult
        }
        try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes == null) {
                onImagePicked(null)
                return@rememberLauncherForActivityResult
            }
            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
            val extension = when {
                mimeType.contains("png") -> "png"
                mimeType.contains("gif") -> "gif"
                mimeType.contains("webp") -> "webp"
                else -> "jpg"
            }
            onImagePicked(ImagePickerResult(bytes, "avatar.$extension", mimeType))
        } catch (e: Exception) {
            onImagePicked(null)
        }
    }
    return {
        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
}
