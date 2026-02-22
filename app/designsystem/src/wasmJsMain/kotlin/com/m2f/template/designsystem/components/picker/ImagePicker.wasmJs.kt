package com.m2f.template.designsystem.components.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader
import org.w3c.files.get

/**
 * WASM (Web) implementation of the image picker using HTML file input.
 */
@Composable
actual fun rememberImagePickerLauncher(
    onImagePicked: (ImagePickerResult?) -> Unit,
): () -> Unit {
    val callback = remember { onImagePicked }

    return {
        val input = document.createElement("input") as HTMLInputElement
        input.type = "file"
        input.accept = "image/*"

        input.onchange = { event ->
            val file = input.files?.get(0)
            if (file == null) {
                callback(null)
            } else {
                val reader = FileReader()
                reader.onload = {
                    val result = reader.result
                    if (result != null) {
                        // result is an ArrayBuffer, convert to ByteArray
                        val arrayBuffer = result.asDynamic()
                        val uint8Array = js("new Uint8Array(arrayBuffer)")
                        val length = uint8Array.length as Int
                        val bytes = ByteArray(length)
                        for (i in 0 until length) {
                            bytes[i] = (uint8Array[i] as Int).toByte()
                        }
                        callback(ImagePickerResult(bytes, file.name, file.type.ifBlank { "image/jpeg" }))
                    } else {
                        callback(null)
                    }
                }
                reader.onerror = {
                    callback(null)
                }
                reader.readAsArrayBuffer(file)
            }
        }

        input.click()
    }
}
