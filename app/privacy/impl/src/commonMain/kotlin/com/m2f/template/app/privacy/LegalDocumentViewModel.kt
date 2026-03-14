package com.m2f.template.app.privacy

import androidx.lifecycle.viewModelScope
import com.m2f.template.core.mvi.MviViewModel
import com.m2f.template.models.dto.privacy.ConsentType
import com.m2f.template.models.localization.StringKey
import com.m2f.template.sdk.Sdk
import kotlinx.coroutines.launch

class LegalDocumentViewModel(
    private val sdk: Sdk,
) : MviViewModel<LegalDocumentIntent, LegalDocumentModel, LegalDocumentMutation, LegalDocumentEvent>(
    initialState = LegalDocumentModel()
) {

    private var lastLoadedType: String? = null

    override fun take(intent: LegalDocumentIntent) {
        viewModelScope.launch {
            when (intent) {
                is LegalDocumentIntent.Load -> {
                    lastLoadedType = intent.type
                    handleLoad(intent.type, intent.locale)
                }
                is LegalDocumentIntent.SwitchLocale -> {
                    val type = lastLoadedType ?: return@launch
                    handleLoad(type, intent.locale)
                }
            }
        }
    }

    private suspend fun handleLoad(type: String, locale: String?) {
        sendMutation(LegalDocumentMutation.SetLoading(true))
        sdk.getLegalDocument(ConsentType.valueOf(type), locale).fold(
            ifLeft = { error ->
                val key = StringKey.fromCode(error.code) ?: StringKey.GENERIC_ERROR
                sendMutation(LegalDocumentMutation.SetError(key))
            },
            ifRight = { document ->
                sendMutation(LegalDocumentMutation.SetDocument(document))
            },
        )
    }

    override suspend fun reduce(model: LegalDocumentModel, mutation: LegalDocumentMutation): LegalDocumentModel =
        when (mutation) {
            is LegalDocumentMutation.SetDocument -> model.copy(
                document = mutation.document,
                loading = false,
                error = null,
            )
            is LegalDocumentMutation.SetLoading -> model.copy(
                loading = mutation.loading,
                error = null,
            )
            is LegalDocumentMutation.SetError -> model.copy(
                error = mutation.error,
                loading = false,
            )
        }
}
