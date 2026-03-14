package com.m2f.template.app.privacy

sealed interface AccountDeletionEvent {
    data object DeletionScheduled : AccountDeletionEvent
    data object DeletionCancelled : AccountDeletionEvent
    data object NavigateToLogin : AccountDeletionEvent
    data class ShowError(val message: String) : AccountDeletionEvent
}
