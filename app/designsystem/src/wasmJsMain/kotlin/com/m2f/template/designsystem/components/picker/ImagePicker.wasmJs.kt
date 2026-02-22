package com.m2f.template.designsystem.components.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.browser.document
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader
import org.w3c.files.get

/**
 * WASM (Web) implementation of the image picker using HTML file input.
 */
@OptIn(ExperimentalWasmJsInterop::class)
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
                        // result is an ArrayBuffer, convert to ByteArray via Uint8Array
                        val arrayBuffer = result.unsafeCast<ArrayBuffer>()
                        val uint8Array = Uint8Array(arrayBuffer)
                        val length = uint8Array.length
                        val bytes = ByteArray(length) { i -> uint8Array[i] }
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
