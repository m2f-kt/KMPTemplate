package com.m2f.template.app.privacy

sealed interface LegalDocumentIntent {
    data class Load(val type: String, val locale: String? = null) : LegalDocumentIntent
    data class SwitchLocale(val locale: String) : LegalDocumentIntent
}
