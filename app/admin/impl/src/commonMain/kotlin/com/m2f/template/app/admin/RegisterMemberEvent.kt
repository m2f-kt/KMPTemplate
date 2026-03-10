package com.m2f.template.app.admin

sealed interface RegisterMemberEvent {
    data object RegistrationSuccess : RegisterMemberEvent
}
