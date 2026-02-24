package com.m2f.template.app.documents

sealed interface DocumentsEvent {
    data object UploadSuccess : DocumentsEvent
}
