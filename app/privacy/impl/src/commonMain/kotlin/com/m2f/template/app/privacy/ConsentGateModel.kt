package com.m2f.template.app.privacy

import com.m2f.template.models.dto.privacy.ConsentType
import com.m2f.template.models.localization.StringKey

data class ConsentItem(
    val type: ConsentType,
    val currentVersion: String,
    val accepted: Boolean,
)

data class ConsentGateModel(
    val consents: List<ConsentItem> = emptyList(),
    val allAccepted: Boolean = false,
    val loading: Boolean = true,
    val error: StringKey? = null,
)
