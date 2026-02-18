package com.m2f.template.app.auth

sealed interface RegisterEvent {
    data object NavigateToDashboard : RegisterEvent
}
