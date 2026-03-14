package com.m2f.template.app.privacy

import com.m2f.template.models.dto.privacy.ConsentType

sealed interface ConsentGateEvent {
    data class NavigateToDocument(val type: ConsentType) : ConsentGateEvent
    data object ConsentCompleted : ConsentGateEvent
    data class ShowError(val message: String) : ConsentGateEvent
}
