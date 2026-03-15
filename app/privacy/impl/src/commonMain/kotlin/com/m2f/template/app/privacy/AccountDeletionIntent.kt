package com.m2f.template.app.privacy

sealed interface AccountDeletionIntent {
    data object Load : AccountDeletionIntent
    data object ProceedToReAuth : AccountDeletionIntent
    data class ReAuthenticate(val password: String) : AccountDeletionIntent
    data class SetReason(val reason: String) : AccountDeletionIntent
    data object ConfirmDeletion : AccountDeletionIntent
    data object CancelDeletion : AccountDeletionIntent
}
