package com.m2f.template.app.admin

import com.m2f.template.models.dto.InvitationResponse
import com.m2f.template.models.dto.MemberResponse

sealed interface AdminPanelIntent {
    data class LoadAdminPanel(val groupId: String) : AdminPanelIntent
    data object LoadMoreMembers : AdminPanelIntent
    data object RegisterMemberClicked : AdminPanelIntent
    data object OpenCreateGroupDialog : AdminPanelIntent
    data object CloseCreateGroupDialog : AdminPanelIntent
    data class CreateGroupNameChanged(val name: String) : AdminPanelIntent
    data object SubmitCreateGroup : AdminPanelIntent
    // Invite member intents
    data object OpenInviteDialog : AdminPanelIntent
    data object CloseInviteDialog : AdminPanelIntent
    data class InviteEmailChanged(val email: String) : AdminPanelIntent
    data object SendInvite : AdminPanelIntent
    // Pending invitations intents
    data object LoadInvitations : AdminPanelIntent
    data class ConfirmRevokeInvitation(val invitation: InvitationResponse) : AdminPanelIntent
    data object CancelRevoke : AdminPanelIntent
    data object ExecuteRevoke : AdminPanelIntent
    data class ResendInvitation(val invitation: InvitationResponse) : AdminPanelIntent
    // Remove member intents
    data class ConfirmRemoveMember(val member: MemberResponse) : AdminPanelIntent
    data object CancelRemoveMember : AdminPanelIntent
    data object ExecuteRemoveMember : AdminPanelIntent
}
