package com.m2f.template.app.privacy

import androidx.lifecycle.viewModelScope
import com.m2f.template.core.mvi.MviViewModel
import com.m2f.template.models.dto.privacy.DeletionRequest
import com.m2f.template.models.localization.StringKey
import com.m2f.template.sdk.Sdk
import kotlinx.coroutines.launch

class AccountDeletionViewModel(
    private val sdk: Sdk,
) : MviViewModel<AccountDeletionIntent, AccountDeletionModel, AccountDeletionMutation, AccountDeletionEvent>(
    initialState = AccountDeletionModel()
) {

    override fun take(intent: AccountDeletionIntent) {
        viewModelScope.launch {
            when (intent) {
                is AccountDeletionIntent.Load -> handleLoad()
                is AccountDeletionIntent.ProceedToReAuth -> handleProceedToReAuth()
                is AccountDeletionIntent.ReAuthenticate -> handleReAuthenticate(intent.password)
                is AccountDeletionIntent.SetReason -> handleSetReason(intent.reason)
                is AccountDeletionIntent.ConfirmDeletion -> handleConfirmDeletion()
                is AccountDeletionIntent.CancelDeletion -> handleCancelDeletion()
                is AccountDeletionIntent.SkipReason -> handleSkipReason()
                is AccountDeletionIntent.LogOut -> handleLogOut()
            }
        }
    }

    private suspend fun handleLoad() {
        sdk.getProfile().fold(
            ifLeft = { /* ignore profile fetch error */ },
            ifRight = { profile ->
                sendMutation(AccountDeletionMutation.SetUserEmail(profile.email))
            },
        )
        sdk.getDeletionStatus().fold(
            ifLeft = { error ->
                val key = StringKey.fromCode(error.code) ?: StringKey.GENERIC_ERROR
                sendMutation(AccountDeletionMutation.SetError(key))
            },
            ifRight = { response ->
                if (response != null) {
                    sendMutation(AccountDeletionMutation.SetPendingDeletion(response))
                    sendMutation(AccountDeletionMutation.SetStep(DeletionStep.SCHEDULED))
                }
            },
        )
    }

    private suspend fun handleSkipReason() {
        sendMutation(AccountDeletionMutation.SetReason(""))
        sendMutation(AccountDeletionMutation.SetStep(DeletionStep.CONFIRM))
    }

    private suspend fun handleLogOut() {
        sdk.logout()
        sendEvent(AccountDeletionEvent.LoggedOut)
    }

    private suspend fun handleProceedToReAuth() {
        sendMutation(AccountDeletionMutation.SetStep(DeletionStep.RE_AUTH))
    }

    private suspend fun handleReAuthenticate(password: String) {
        sendMutation(AccountDeletionMutation.SetPassword(password))
        sendMutation(AccountDeletionMutation.SetStep(DeletionStep.REASON))
    }

    private suspend fun handleSetReason(reason: String) {
        sendMutation(AccountDeletionMutation.SetReason(reason))
        sendMutation(AccountDeletionMutation.SetStep(DeletionStep.CONFIRM))
    }

    private suspend fun handleConfirmDeletion() {
        sendMutation(AccountDeletionMutation.SetLoading(true))
        val currentModel = model.value
        sdk.requestAccountDeletion(
            DeletionRequest(
                password = currentModel.password,
                reason = currentModel.reason,
            )
        ).fold(
            ifLeft = { error ->
                val key = StringKey.fromCode(error.code) ?: StringKey.GENERIC_ERROR
                sendMutation(AccountDeletionMutation.SetError(key))
            },
            ifRight = {
                sendMutation(AccountDeletionMutation.SetLoading(false))
                sdk.logout()
                sendEvent(AccountDeletionEvent.NavigateToLogin)
            },
        )
    }

    private suspend fun handleCancelDeletion() {
        sendMutation(AccountDeletionMutation.SetLoading(true))
        sdk.cancelDeletion().fold(
            ifLeft = { error ->
                val key = StringKey.fromCode(error.code) ?: StringKey.GENERIC_ERROR
                sendMutation(AccountDeletionMutation.SetError(key))
            },
            ifRight = {
                sendMutation(AccountDeletionMutation.SetLoading(false))
                sendMutation(AccountDeletionMutation.SetPendingDeletion(null))
                sendMutation(AccountDeletionMutation.SetStep(DeletionStep.WARNING))
                sendEvent(AccountDeletionEvent.DeletionCancelled)
            },
        )
    }

    override suspend fun reduce(
        model: AccountDeletionModel,
        mutation: AccountDeletionMutation,
    ): AccountDeletionModel = when (mutation) {
        is AccountDeletionMutation.SetStep -> model.copy(step = mutation.step)
        is AccountDeletionMutation.SetPassword -> model.copy(password = mutation.password)
        is AccountDeletionMutation.SetReason -> model.copy(reason = mutation.reason)
        is AccountDeletionMutation.SetPendingDeletion -> model.copy(pendingDeletion = mutation.deletion)
        is AccountDeletionMutation.SetLoading -> model.copy(loading = mutation.loading, error = null)
        is AccountDeletionMutation.SetError -> model.copy(error = mutation.error, loading = false)
        is AccountDeletionMutation.SetUserEmail -> model.copy(userEmail = mutation.email)
    }
}
