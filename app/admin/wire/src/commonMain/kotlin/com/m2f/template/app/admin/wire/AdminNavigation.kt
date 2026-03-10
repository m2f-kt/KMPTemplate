package com.m2f.template.app.admin.wire

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import com.m2f.template.app.admin.AdminPanelEvent
import com.m2f.template.app.admin.AdminPanelIntent
import com.m2f.template.app.admin.AdminPanelScreen
import com.m2f.template.app.admin.AdminPanelViewModel
import com.m2f.template.app.admin.RegisterMemberEvent
import com.m2f.template.app.admin.RegisterMemberIntent
import com.m2f.template.app.admin.RegisterMemberScreen
import com.m2f.template.app.admin.RegisterMemberViewModel
import com.m2f.template.app.admin.contract.AdminPanelRoute
import com.m2f.template.app.admin.contract.RegisterMemberRoute
import com.m2f.template.navigation.Route
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<Route>.adminEntries(
    backStack: MutableList<Route>,
) {
    entry<AdminPanelRoute> { route ->
        val viewModel = koinViewModel<AdminPanelViewModel>()
        val state by viewModel.model.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            val groupId = route.groupId
            if (groupId != null) {
                viewModel.take(AdminPanelIntent.LoadAdminPanel(groupId))
            }
        }

        AdminPanelScreen(
            state = state,
            onLoadMore = { viewModel.take(AdminPanelIntent.LoadMoreMembers) },
            onRegisterMember = { viewModel.take(AdminPanelIntent.RegisterMemberClicked) },
            onBack = { backStack.removeLastOrNull() },
            onOpenCreateGroup = { viewModel.take(AdminPanelIntent.OpenCreateGroupDialog) },
            onCloseCreateGroup = { viewModel.take(AdminPanelIntent.CloseCreateGroupDialog) },
            onCreateGroupNameChange = { viewModel.take(AdminPanelIntent.CreateGroupNameChanged(it)) },
            onSubmitCreateGroup = { viewModel.take(AdminPanelIntent.SubmitCreateGroup) },
            onOpenInvite = { viewModel.take(AdminPanelIntent.OpenInviteDialog) },
            onCloseInvite = { viewModel.take(AdminPanelIntent.CloseInviteDialog) },
            onInviteEmailChange = { viewModel.take(AdminPanelIntent.InviteEmailChanged(it)) },
            onSendInvite = { viewModel.take(AdminPanelIntent.SendInvite) },
            onConfirmRevoke = { viewModel.take(AdminPanelIntent.ConfirmRevokeInvitation(it)) },
            onCancelRevoke = { viewModel.take(AdminPanelIntent.CancelRevoke) },
            onExecuteRevoke = { viewModel.take(AdminPanelIntent.ExecuteRevoke) },
            onResend = { viewModel.take(AdminPanelIntent.ResendInvitation(it)) },
            onConfirmRemoveMember = { viewModel.take(AdminPanelIntent.ConfirmRemoveMember(it)) },
            onCancelRemoveMember = { viewModel.take(AdminPanelIntent.CancelRemoveMember) },
            onExecuteRemoveMember = { viewModel.take(AdminPanelIntent.ExecuteRemoveMember) },
        )
        LaunchedEffect(Unit) {
            viewModel.event.collect { event ->
                when (event) {
                    is AdminPanelEvent.NavigateToRegisterMember -> {
                        backStack.add(RegisterMemberRoute(groupId = event.groupId))
                    }
                    is AdminPanelEvent.GroupCreated -> {
                        backStack.add(AdminPanelRoute(groupId = event.groupId))
                    }
                }
            }
        }
    }

    entry<RegisterMemberRoute> { route ->
        val viewModel = koinViewModel<RegisterMemberViewModel>()
        val state by viewModel.model.collectAsStateWithLifecycle()
        RegisterMemberScreen(
            state = state,
            onEmailChange = { viewModel.take(RegisterMemberIntent.EmailChanged(it)) },
            onPasswordChange = { viewModel.take(RegisterMemberIntent.PasswordChanged(it)) },
            onFirstNameChange = { viewModel.take(RegisterMemberIntent.FirstNameChanged(it)) },
            onLastNameChange = { viewModel.take(RegisterMemberIntent.LastNameChanged(it)) },
            onRoleChange = { viewModel.take(RegisterMemberIntent.RoleChanged(it)) },
            onSubmit = { viewModel.take(RegisterMemberIntent.SubmitRegisterMember(route.groupId)) },
            onBack = { backStack.removeLastOrNull() },
        )
        LaunchedEffect(Unit) {
            viewModel.event.collect { event ->
                when (event) {
                    is RegisterMemberEvent.RegistrationSuccess -> {
                        backStack.removeLastOrNull()
                    }
                }
            }
        }
    }
}
