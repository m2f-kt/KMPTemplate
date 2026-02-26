package com.m2f.template.app.admin

import androidx.lifecycle.viewModelScope
import com.m2f.template.core.mvi.MviViewModel
import com.m2f.template.models.dto.CreateGroupRequest
import com.m2f.template.models.dto.CreateInvitationRequest
import com.m2f.template.models.localization.StringKey
import com.m2f.template.sdk.Sdk
import kotlinx.coroutines.launch

class AdminPanelViewModel(
    private val sdk: Sdk,
) : MviViewModel<AdminPanelIntent, AdminPanelModel, AdminPanelMutation, AdminPanelEvent>(
    initialState = AdminPanelModel(),
) {

    override fun take(intent: AdminPanelIntent) {
        viewModelScope.launch {
            when (intent) {
                is AdminPanelIntent.LoadAdminPanel -> handleLoadAdminPanel(intent.groupId)
                is AdminPanelIntent.LoadMoreMembers -> handleLoadMoreMembers()
                is AdminPanelIntent.RegisterMemberClicked -> {
                    val groupId = model.value.groupId
                    if (groupId.isNotBlank()) {
                        sendEvent(AdminPanelEvent.NavigateToRegisterMember(groupId))
                    }
                }
                is AdminPanelIntent.OpenCreateGroupDialog -> sendMutation(AdminPanelMutation.ShowCreateGroupDialog)
                is AdminPanelIntent.CloseCreateGroupDialog -> sendMutation(AdminPanelMutation.HideCreateGroupDialog)
                is AdminPanelIntent.CreateGroupNameChanged -> sendMutation(AdminPanelMutation.SetCreateGroupName(intent.name))
                is AdminPanelIntent.SubmitCreateGroup -> handleCreateGroup()
                // Invite member handling
                is AdminPanelIntent.OpenInviteDialog -> sendMutation(AdminPanelMutation.ShowInviteDialog)
                is AdminPanelIntent.CloseInviteDialog -> sendMutation(AdminPanelMutation.HideInviteDialog)
                is AdminPanelIntent.InviteEmailChanged -> sendMutation(AdminPanelMutation.SetInviteEmail(intent.email))
                is AdminPanelIntent.SendInvite -> handleSendInvite()
                // Pending invitations handling
                is AdminPanelIntent.LoadInvitations -> handleLoadInvitations()
                is AdminPanelIntent.ConfirmRevokeInvitation -> sendMutation(AdminPanelMutation.ShowRevokeDialog(intent.invitation))
                is AdminPanelIntent.CancelRevoke -> sendMutation(AdminPanelMutation.HideRevokeDialog)
                is AdminPanelIntent.ExecuteRevoke -> handleRevokeInvitation()
            }
        }
    }

    private suspend fun handleLoadAdminPanel(groupId: String) {
        sendMutation(AdminPanelMutation.SetLoading(true))
        sdk.getGroup(groupId).fold(
            ifLeft = { error ->
                sendMutation(AdminPanelMutation.SetError(StringKey.fromCode(error.code) ?: StringKey.GENERIC_ERROR))
                sendMutation(AdminPanelMutation.SetLoading(false))
            },
            ifRight = { group ->
                sendMutation(
                    AdminPanelMutation.SetGroupInfo(
                        groupId = group.id,
                        groupName = group.name,
                        groupSlug = group.slug,
                        groupDescription = group.description,
                        memberCount = group.memberCount,
                    ),
                )
                sdk.getMembers(groupId, cursor = null, limit = 20).fold(
                    ifLeft = { error ->
                        sendMutation(AdminPanelMutation.SetError(StringKey.fromCode(error.code) ?: StringKey.GENERIC_ERROR))
                        sendMutation(AdminPanelMutation.SetLoading(false))
                    },
                    ifRight = { page ->
                        sendMutation(
                            AdminPanelMutation.SetMembers(
                                members = page.items,
                                cursor = page.cursor,
                                hasMore = page.hasMore,
                            ),
                        )
                        sendMutation(AdminPanelMutation.SetLoading(false))
                        // Auto-load invitations after admin panel loads
                        handleLoadInvitations()
                    },
                )
            },
        )
    }

    private suspend fun handleLoadMoreMembers() {
        val current = model.value
        if (current.isLoadingMore || !current.hasMoreMembers || current.membersCursor == null) return
        sendMutation(AdminPanelMutation.SetLoadingMore(true))
        sdk.getMembers(current.groupId, cursor = current.membersCursor, limit = 20).fold(
            ifLeft = { error ->
                sendMutation(AdminPanelMutation.SetError(StringKey.fromCode(error.code) ?: StringKey.GENERIC_ERROR))
                sendMutation(AdminPanelMutation.SetLoadingMore(false))
            },
            ifRight = { page ->
                sendMutation(
                    AdminPanelMutation.AppendMembers(
                        members = page.items,
                        cursor = page.cursor,
                        hasMore = page.hasMore,
                    ),
                )
                sendMutation(AdminPanelMutation.SetLoadingMore(false))
            },
        )
    }

    private suspend fun handleCreateGroup() {
        val current = model.value
        val name = current.createGroupName.trim()

        if (name.isBlank()) {
            sendMutation(AdminPanelMutation.SetCreateGroupError(StringKey.VALIDATION_NAME_BLANK))
            return
        }

        sendMutation(AdminPanelMutation.SetCreatingGroup(true))
        sendMutation(AdminPanelMutation.SetCreateGroupError(null))

        // Generate slug from name: lowercase, replace spaces with hyphens
        val slug = name.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')

        sdk.createGroup(CreateGroupRequest(name = name, slug = slug)).fold(
            ifLeft = { error ->
                sendMutation(AdminPanelMutation.SetCreateGroupError(StringKey.fromCode(error.code) ?: StringKey.GENERIC_ERROR))
                sendMutation(AdminPanelMutation.SetCreatingGroup(false))
            },
            ifRight = { group ->
                sendMutation(AdminPanelMutation.SetCreatingGroup(false))
                sendMutation(AdminPanelMutation.SetCreateGroupSuccess)
                sendEvent(AdminPanelEvent.GroupCreated(group.id, group.slug))
            },
        )
    }

    private suspend fun handleSendInvite() {
        val current = model.value
        val email = current.inviteEmail.trim()

        if (email.isBlank() || !email.contains("@")) {
            sendMutation(AdminPanelMutation.SetInviteError(StringKey.VALIDATION_EMAIL_INVALID))
            return
        }

        sendMutation(AdminPanelMutation.SetSendingInvite(true))
        sendMutation(AdminPanelMutation.SetInviteError(null))

        sdk.createInvitation(current.groupId, CreateInvitationRequest(email = email)).fold(
            ifLeft = { error ->
                sendMutation(AdminPanelMutation.SetInviteError(StringKey.fromCode(error.code) ?: StringKey.GENERIC_ERROR))
                sendMutation(AdminPanelMutation.SetSendingInvite(false))
            },
            ifRight = { invitation ->
                // Invitation sent successfully - link was emailed to the invitee
                sendMutation(AdminPanelMutation.SetInviteSuccess)
                sendMutation(AdminPanelMutation.SetSendingInvite(false))
            },
        )
    }

    private suspend fun handleLoadInvitations() {
        val groupId = model.value.groupId
        if (groupId.isBlank()) return
        sendMutation(AdminPanelMutation.SetLoadingInvitations(true))
        sdk.listInvitations(groupId).fold(
            ifLeft = { /* silently ignore -- invitations section is secondary */ },
            ifRight = { invitations -> sendMutation(AdminPanelMutation.SetInvitations(invitations)) },
        )
        sendMutation(AdminPanelMutation.SetLoadingInvitations(false))
    }

    private suspend fun handleRevokeInvitation() {
        val target = model.value.revokeTarget ?: return
        sendMutation(AdminPanelMutation.SetRevoking(true))
        sdk.revokeInvitation(model.value.groupId, target.id).fold(
            ifLeft = {
                sendMutation(AdminPanelMutation.SetRevoking(false))
                sendMutation(AdminPanelMutation.HideRevokeDialog)
            },
            ifRight = {
                sendMutation(AdminPanelMutation.SetRevoking(false))
                sendMutation(AdminPanelMutation.HideRevokeDialog)
                handleLoadInvitations() // Refresh the list
            },
        )
    }

    override suspend fun reduce(
        model: AdminPanelModel,
        mutation: AdminPanelMutation,
    ): AdminPanelModel = when (mutation) {
        is AdminPanelMutation.SetLoading -> model.copy(isLoading = mutation.loading)
        is AdminPanelMutation.SetLoadingMore -> model.copy(isLoadingMore = mutation.loading)
        is AdminPanelMutation.SetGroupInfo -> model.copy(
            groupId = mutation.groupId,
            groupName = mutation.groupName,
            groupSlug = mutation.groupSlug,
            groupDescription = mutation.groupDescription,
            memberCount = mutation.memberCount,
        )
        is AdminPanelMutation.SetMembers -> model.copy(
            members = mutation.members,
            membersCursor = mutation.cursor,
            hasMoreMembers = mutation.hasMore,
        )
        is AdminPanelMutation.AppendMembers -> model.copy(
            members = model.members + mutation.members,
            membersCursor = mutation.cursor,
            hasMoreMembers = mutation.hasMore,
        )
        is AdminPanelMutation.SetError -> model.copy(error = mutation.error)
        is AdminPanelMutation.ShowCreateGroupDialog -> model.copy(
            showCreateGroupDialog = true,
            createGroupName = "",
            createGroupError = null,
            createGroupSuccess = false,
        )
        is AdminPanelMutation.HideCreateGroupDialog -> model.copy(
            showCreateGroupDialog = false,
            createGroupName = "",
            createGroupError = null,
        )
        is AdminPanelMutation.SetCreateGroupName -> model.copy(createGroupName = mutation.name)
        is AdminPanelMutation.SetCreatingGroup -> model.copy(isCreatingGroup = mutation.creating)
        is AdminPanelMutation.SetCreateGroupError -> model.copy(createGroupError = mutation.error)
        is AdminPanelMutation.SetCreateGroupSuccess -> model.copy(
            showCreateGroupDialog = false,
            createGroupSuccess = true,
        )
        // Invite member mutations
        is AdminPanelMutation.ShowInviteDialog -> model.copy(
            showInviteDialog = true,
            inviteEmail = "",
            inviteError = null,
            inviteSuccess = false,
        )
        is AdminPanelMutation.HideInviteDialog -> model.copy(
            showInviteDialog = false,
            inviteEmail = "",
            inviteError = null,
        )
        is AdminPanelMutation.SetInviteEmail -> model.copy(inviteEmail = mutation.email)
        is AdminPanelMutation.SetSendingInvite -> model.copy(isSendingInvite = mutation.sending)
        is AdminPanelMutation.SetInviteError -> model.copy(inviteError = mutation.error)
        is AdminPanelMutation.SetInviteSuccess -> model.copy(
            inviteSuccess = true,
        )
        // Pending invitations mutations
        is AdminPanelMutation.SetInvitations -> model.copy(invitations = mutation.invitations)
        is AdminPanelMutation.SetLoadingInvitations -> model.copy(isLoadingInvitations = mutation.loading)
        is AdminPanelMutation.ShowRevokeDialog -> model.copy(showRevokeDialog = true, revokeTarget = mutation.invitation)
        is AdminPanelMutation.HideRevokeDialog -> model.copy(showRevokeDialog = false, revokeTarget = null, isRevoking = false)
        is AdminPanelMutation.SetRevoking -> model.copy(isRevoking = mutation.revoking)
    }
}
