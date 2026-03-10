package com.m2f.template.app.profile

sealed interface ProfileEvent {
    data object NavigateToLogin : ProfileEvent
}
