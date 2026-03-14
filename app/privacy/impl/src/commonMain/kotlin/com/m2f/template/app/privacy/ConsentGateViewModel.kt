package com.m2f.template.app.privacy

import androidx.lifecycle.viewModelScope
import com.m2f.template.core.mvi.MviViewModel
import com.m2f.template.models.dto.privacy.GrantConsentRequest
import com.m2f.template.models.localization.StringKey
import com.m2f.template.sdk.Sdk
import kotlinx.coroutines.launch

class ConsentGateViewModel(
    private val sdk: Sdk,
) : MviViewModel<ConsentGateIntent, ConsentGateModel, ConsentGateMutation, ConsentGateEvent>(
    initialState = ConsentGateModel()
) {

    override fun take(intent: ConsentGateIntent) {
        viewModelScope.launch {
            when (intent) {
                is ConsentGateIntent.LoadRequiredConsents -> handleLoadConsents()
                is ConsentGateIntent.ToggleConsent -> handleToggle(intent)
                is ConsentGateIntent.AcceptAll -> handleAcceptAll()
                is ConsentGateIntent.ViewDocument -> sendEvent(ConsentGateEvent.NavigateToDocument(intent.type))
            }
        }
    }

    private suspend fun handleLoadConsents() {
        sendMutation(ConsentGateMutation.SetLoading(true))
        sdk.getRequiredConsents().fold(
            ifLeft = { error ->
                val key = StringKey.fromCode(error.code) ?: StringKey.GENERIC_ERROR
                sendMutation(ConsentGateMutation.SetError(key))
            },
            ifRight = { response ->
                val items = response.consents.map { consent ->
                    ConsentItem(
                        type = consent.type,
                        currentVersion = consent.currentVersion,
                        accepted = !consent.needsUpdate,
                    )
                }
                sendMutation(ConsentGateMutation.SetConsents(items))
            },
        )
    }

    private suspend fun handleToggle(intent: ConsentGateIntent.ToggleConsent) {
        val current = model.value.consents.find { it.type == intent.type } ?: return
        sendMutation(ConsentGateMutation.UpdateConsentToggle(intent.type, !current.accepted))
    }

    private suspend fun handleAcceptAll() {
        sendMutation(ConsentGateMutation.SetLoading(true))
        val consents = model.value.consents
        var allSucceeded = true
        for (consent in consents) {
            sdk.grantConsent(GrantConsentRequest(consent.type, consent.currentVersion))
                .fold(
                    ifLeft = {
                        allSucceeded = false
                        val key = StringKey.fromCode(it.code) ?: StringKey.GENERIC_ERROR
                        sendMutation(ConsentGateMutation.SetError(key))
                    },
                    ifRight = { },
                )
            if (!allSucceeded) break
        }
        if (allSucceeded) {
            sendEvent(ConsentGateEvent.ConsentCompleted)
        }
    }

    override suspend fun reduce(model: ConsentGateModel, mutation: ConsentGateMutation): ConsentGateModel =
        when (mutation) {
            is ConsentGateMutation.SetConsents -> model.copy(
                consents = mutation.consents,
                allAccepted = mutation.consents.all { it.accepted },
                loading = false,
                error = null,
            )
            is ConsentGateMutation.UpdateConsentToggle -> {
                val updated = model.consents.map {
                    if (it.type == mutation.type) it.copy(accepted = mutation.accepted) else it
                }
                model.copy(consents = updated, allAccepted = updated.all { it.accepted })
            }
            is ConsentGateMutation.SetLoading -> model.copy(loading = mutation.loading, error = null)
            is ConsentGateMutation.SetError -> model.copy(error = mutation.error, loading = false)
        }
}
