package com.m2f.template.designsystem.components.picker

import androidx.compose.runtime.Composable

/**
 * Result from the image picker containing the selected image data.
 *
 * @property bytes The raw bytes of the selected image.
 * @property fileName The name of the selected file.
 * @property mimeType The MIME type of the image (e.g., "image/jpeg", "image/png").
 */
data class ImagePickerResult(
    val bytes: ByteArray,
    val fileName: String,
    val mimeType: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ImagePickerResult
        if (!bytes.contentEquals(other.bytes)) return false
        if (fileName != other.fileName) return false
        if (mimeType != other.mimeType) return false
        return true
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + mimeType.hashCode()
        return result
    }
}

/**
 * Creates and remembers an image picker launcher that invokes the platform-specific
 * image selection UI.
 *
 * @param onImagePicked Callback invoked when the user selects an image. Receives null
 *        if the user cancels the picker.
 * @return A function that launches the image picker when called.
 */
@Composable
expect fun rememberImagePickerLauncher(
    onImagePicked: (ImagePickerResult?) -> Unit,
): () -> Unit
