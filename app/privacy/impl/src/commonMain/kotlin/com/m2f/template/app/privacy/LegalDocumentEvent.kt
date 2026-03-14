package com.m2f.template.app.privacy

sealed interface LegalDocumentEvent {
    data class ShowError(val message: String) : LegalDocumentEvent
}
