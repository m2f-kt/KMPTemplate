package com.m2f.template.app.profile.wire

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import com.m2f.template.app.auth.contract.LoginRoute
import com.m2f.template.app.privacy.contract.PrivacySettingsRoute
import com.m2f.template.app.profile.ProfileEvent
import com.m2f.template.app.profile.ProfileIntent
import com.m2f.template.app.profile.ProfileScreen
import com.m2f.template.app.profile.ProfileViewModel
import com.m2f.template.app.profile.contract.ProfileRoute
import com.m2f.template.navigation.Route
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<Route>.profileEntries(
    backStack: MutableList<Route>,
    localeSelector: @Composable () -> Unit,
) {
    entry<ProfileRoute> {
        val viewModel = koinViewModel<ProfileViewModel>()
        val state by viewModel.model.collectAsStateWithLifecycle()

        ProfileScreen(
            state = state,
            onStartEditing = { viewModel.take(ProfileIntent.StartEditing) },
            onCancelEditing = { viewModel.take(ProfileIntent.CancelEditing) },
            onEditNameChange = { viewModel.take(ProfileIntent.EditNameChanged(it)) },
            onEditEmailChange = { viewModel.take(ProfileIntent.EditEmailChanged(it)) },
            onSaveProfile = { viewModel.take(ProfileIntent.SaveProfileClicked) },
            onLogout = { viewModel.take(ProfileIntent.LogoutClicked) },
            onBack = { backStack.removeLastOrNull() },
            onImageSelected = { bytes, mimeType ->
                viewModel.take(ProfileIntent.ImageSelected(bytes, mimeType))
            },
            onCropConfirmed = { viewModel.take(ProfileIntent.CropConfirmed) },
            onCropCancelled = { viewModel.take(ProfileIntent.CropCancelled) },
            onNavigateToPrivacy = { backStack.add(PrivacySettingsRoute) },
            localeSelector = { localeSelector() },
        )
        LaunchedEffect(Unit) {
            viewModel.event.collect { event ->
                when (event) {
                    is ProfileEvent.NavigateToLogin -> {
                        backStack.clear()
                        backStack.add(LoginRoute())
                    }
                    is ProfileEvent.NavigateToPrivacySettings -> {
                        backStack.add(PrivacySettingsRoute)
                    }
                }
            }
        }
    }
}
