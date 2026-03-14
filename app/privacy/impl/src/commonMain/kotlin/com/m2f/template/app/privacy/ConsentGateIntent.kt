package com.m2f.template.app.privacy

import com.m2f.template.models.dto.privacy.ConsentType

sealed interface ConsentGateIntent {
    data object LoadRequiredConsents : ConsentGateIntent
    data class ToggleConsent(val type: ConsentType) : ConsentGateIntent
    data object AcceptAll : ConsentGateIntent
    data class ViewDocument(val type: ConsentType) : ConsentGateIntent
}
