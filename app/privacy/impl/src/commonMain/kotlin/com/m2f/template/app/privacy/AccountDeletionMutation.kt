package com.m2f.template.app.privacy

import com.m2f.template.models.dto.privacy.DeletionResponse
import com.m2f.template.models.localization.StringKey

sealed interface AccountDeletionMutation {
    data class SetStep(val step: DeletionStep) : AccountDeletionMutation
    data class SetPassword(val password: String) : AccountDeletionMutation
    data class SetReason(val reason: String) : AccountDeletionMutation
    data class SetPendingDeletion(val deletion: DeletionResponse?) : AccountDeletionMutation
    data class SetLoading(val loading: Boolean) : AccountDeletionMutation
    data class SetError(val error: StringKey?) : AccountDeletionMutation
    data class SetUserEmail(val email: String) : AccountDeletionMutation
}
