package com.m2f.template.app.privacy.wire

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import com.m2f.template.app.privacy.AccountDeletionEvent
import com.m2f.template.app.privacy.AccountDeletionIntent
import com.m2f.template.app.privacy.AccountDeletionScreen
import com.m2f.template.app.privacy.AccountDeletionViewModel
import com.m2f.template.app.privacy.ConsentGateEvent
import com.m2f.template.app.privacy.ConsentGateIntent
import com.m2f.template.app.privacy.ConsentGateScreen
import com.m2f.template.app.privacy.ConsentGateViewModel
import com.m2f.template.app.privacy.LegalDocumentEvent
import com.m2f.template.app.privacy.LegalDocumentIntent
import com.m2f.template.app.privacy.LegalDocumentScreen
import com.m2f.template.app.privacy.LegalDocumentViewModel
import com.m2f.template.app.privacy.PrivacySettingsEvent
import com.m2f.template.app.privacy.PrivacySettingsIntent
import com.m2f.template.app.privacy.PrivacySettingsScreen
import com.m2f.template.app.privacy.PrivacySettingsViewModel
import com.m2f.template.app.privacy.contract.AccountDeletionRoute
import com.m2f.template.app.privacy.contract.ConsentGateRoute
import com.m2f.template.app.privacy.contract.LegalDocumentRoute
import com.m2f.template.app.privacy.contract.PrivacySettingsRoute
import com.m2f.template.navigation.Route
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<Route>.privacyEntries(
    backStack: MutableList<Route>,
    onConsentCompleted: () -> Unit,
    onAccountDeleted: () -> Unit,
) {
    entry<ConsentGateRoute> {
        val viewModel = koinViewModel<ConsentGateViewModel>()
        val state by viewModel.model.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            viewModel.take(ConsentGateIntent.LoadRequiredConsents)
        }

        ConsentGateScreen(
            state = state,
            onToggleConsent = { viewModel.take(ConsentGateIntent.ToggleConsent(it)) },
            onAcceptAll = { viewModel.take(ConsentGateIntent.AcceptAll) },
            onViewDocument = { viewModel.take(ConsentGateIntent.ViewDocument(it)) },
            onDecline = { onAccountDeleted() },
        )

        LaunchedEffect(Unit) {
            viewModel.event.collect { event ->
                when (event) {
                    is ConsentGateEvent.NavigateToDocument -> {
                        backStack.add(LegalDocumentRoute(type = event.type.name))
                    }
                    is ConsentGateEvent.ConsentCompleted -> {
                        onConsentCompleted()
                    }
                    is ConsentGateEvent.ShowError -> {
                        // Error is displayed in the UI via model state
                    }
                }
            }
        }
    }

    entry<PrivacySettingsRoute> {
        val viewModel = koinViewModel<PrivacySettingsViewModel>()
        val state by viewModel.model.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            viewModel.take(PrivacySettingsIntent.Load)
        }

        PrivacySettingsScreen(
            state = state,
            onRequestExport = { viewModel.take(PrivacySettingsIntent.RequestExport) },
            onDownloadExport = { viewModel.take(PrivacySettingsIntent.DownloadExport) },
            onRequestDeletion = { backStack.add(AccountDeletionRoute) },
            onViewDocument = { viewModel.take(PrivacySettingsIntent.ViewDocument(it)) },
            onWithdrawConsent = { viewModel.take(PrivacySettingsIntent.WithdrawConsent(it)) },
            onBack = { backStack.removeLastOrNull() },
        )

        LaunchedEffect(Unit) {
            viewModel.event.collect { event ->
                when (event) {
                    is PrivacySettingsEvent.NavigateToDeletion -> {
                        backStack.add(AccountDeletionRoute)
                    }
                    is PrivacySettingsEvent.NavigateToDocument -> {
                        backStack.add(LegalDocumentRoute(type = event.type.name))
                    }
                    is PrivacySettingsEvent.ExportReady -> {
                        // Export download URL handled via model state
                    }
                    is PrivacySettingsEvent.ShowError -> {
                        // Error is displayed in the UI via model state
                    }
                }
            }
        }
    }

    entry<LegalDocumentRoute> { route ->
        val viewModel = koinViewModel<LegalDocumentViewModel>()
        val state by viewModel.model.collectAsStateWithLifecycle()

        LaunchedEffect(route.type, route.locale) {
            viewModel.take(LegalDocumentIntent.Load(type = route.type, locale = route.locale))
        }

        LegalDocumentScreen(
            state = state,
            onSwitchLocale = { viewModel.take(LegalDocumentIntent.SwitchLocale(it)) },
            onBack = { backStack.removeLastOrNull() },
        )

        LaunchedEffect(Unit) {
            viewModel.event.collect { event ->
                when (event) {
                    is LegalDocumentEvent.ShowError -> {
                        // Error is displayed in the UI via model state
                    }
                }
            }
        }
    }

    entry<AccountDeletionRoute> {
        val viewModel = koinViewModel<AccountDeletionViewModel>()
        val state by viewModel.model.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            viewModel.take(AccountDeletionIntent.Load)
        }

        AccountDeletionScreen(
            state = state,
            onReAuthenticate = { viewModel.take(AccountDeletionIntent.ReAuthenticate(it)) },
            onSetReason = { viewModel.take(AccountDeletionIntent.SetReason(it)) },
            onConfirmDeletion = { viewModel.take(AccountDeletionIntent.ConfirmDeletion) },
            onCancelDeletion = { viewModel.take(AccountDeletionIntent.CancelDeletion) },
            onBack = { backStack.removeLastOrNull() },
        )

        LaunchedEffect(Unit) {
            viewModel.event.collect { event ->
                when (event) {
                    is AccountDeletionEvent.DeletionScheduled -> {
                        backStack.removeLastOrNull()
                    }
                    is AccountDeletionEvent.DeletionCancelled -> {
                        backStack.removeLastOrNull()
                    }
                    is AccountDeletionEvent.NavigateToLogin -> {
                        onAccountDeleted()
                    }
                    is AccountDeletionEvent.ShowError -> {
                        // Error is displayed in the UI via model state
                    }
                }
            }
        }
    }
}
