package com.m2f.template.app.profile

sealed interface ProfileIntent {
    data object LoadProfile : ProfileIntent
    data object StartEditing : ProfileIntent
    data object CancelEditing : ProfileIntent
    data class EditNameChanged(val name: String) : ProfileIntent
    data class EditEmailChanged(val email: String) : ProfileIntent
    data object SaveProfileClicked : ProfileIntent
    data object LogoutClicked : ProfileIntent
}
