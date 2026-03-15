package com.m2f.template.app.privacy

import com.m2f.template.models.dto.privacy.DeletionResponse
import com.m2f.template.models.localization.StringKey

enum class DeletionStep {
    WARNING,
    RE_AUTH,
    REASON,
    CONFIRM,
    SCHEDULED,
}

data class AccountDeletionModel(
    val step: DeletionStep = DeletionStep.WARNING,
    val confirmationToken: String = "",
    val reason: String = "",
    val userEmail: String = "",
    val pendingDeletion: DeletionResponse? = null,
    val loading: Boolean = false,
    val error: StringKey? = null,
)
