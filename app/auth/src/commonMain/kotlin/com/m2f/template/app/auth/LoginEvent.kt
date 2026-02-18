package com.m2f.template.app.auth

sealed interface LoginEvent {
    data object NavigateToDashboard : LoginEvent
}
