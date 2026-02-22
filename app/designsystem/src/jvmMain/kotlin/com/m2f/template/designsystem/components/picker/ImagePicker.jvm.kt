package com.m2f.template.designsystem.components.picker

import androidx.compose.runtime.Composable
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * JVM (Desktop) implementation of the image picker using Swing JFileChooser.
 */
@Composable
actual fun rememberImagePickerLauncher(
    onImagePicked: (ImagePickerResult?) -> Unit,
): () -> Unit = {
    val fileChooser = JFileChooser().apply {
        fileFilter = FileNameExtensionFilter("Images", "jpg", "jpeg", "png", "gif", "webp")
        isAcceptAllFileFilterUsed = false
    }

    if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        val file: File = fileChooser.selectedFile
        try {
            val bytes = file.readBytes()
            val extension = file.extension.lowercase()
            val mimeType = when (extension) {
                "png" -> "image/png"
                "gif" -> "image/gif"
                "webp" -> "image/webp"
                else -> "image/jpeg"
            }
            onImagePicked(ImagePickerResult(bytes, file.name, mimeType))
        } catch (e: Exception) {
            onImagePicked(null)
        }
    } else {
        onImagePicked(null)
    }
}
