package com.m2f.template.app.auth

sealed interface RegisterEvent {
    data object NavigateToDashboard : RegisterEvent
    data class NavigateToGroup(val groupId: String) : RegisterEvent
    data object NavigateToConsentGate : RegisterEvent
    data class ViewLegalDocument(val type: String) : RegisterEvent
}
