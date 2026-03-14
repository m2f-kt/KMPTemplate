package com.m2f.template.app.privacy

import com.m2f.template.models.dto.privacy.ConsentType
import com.m2f.template.models.localization.StringKey

sealed interface ConsentGateMutation {
    data class SetConsents(val consents: List<ConsentItem>) : ConsentGateMutation
    data class UpdateConsentToggle(val type: ConsentType, val accepted: Boolean) : ConsentGateMutation
    data class SetLoading(val loading: Boolean) : ConsentGateMutation
    data class SetError(val error: StringKey?) : ConsentGateMutation
}
