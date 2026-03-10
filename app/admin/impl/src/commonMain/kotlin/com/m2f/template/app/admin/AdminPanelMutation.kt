package com.m2f.template.app.admin

import com.m2f.template.models.dto.InvitationResponse
import com.m2f.template.models.dto.MemberResponse
import com.m2f.template.models.localization.StringKey

sealed interface AdminPanelMutation {
    data class SetLoading(val loading: Boolean) : AdminPanelMutation
    data class SetLoadingMore(val loading: Boolean) : AdminPanelMutation
    data class SetGroupInfo(
        val groupId: String,
        val groupName: String,
        val groupSlug: String,
        val groupDescription: String,
        val memberCount: Int,
    ) : AdminPanelMutation
    data class SetMembers(
        val members: List<MemberResponse>,
        val cursor: String?,
        val hasMore: Boolean,
    ) : AdminPanelMutation
    data class AppendMembers(
        val members: List<MemberResponse>,
        val cursor: String?,
        val hasMore: Boolean,
    ) : AdminPanelMutation
    data class SetError(val error: StringKey) : AdminPanelMutation
    // Create group dialog mutations
    data object ShowCreateGroupDialog : AdminPanelMutation
    data object HideCreateGroupDialog : AdminPanelMutation
    data class SetCreateGroupName(val name: String) : AdminPanelMutation
    data class SetCreatingGroup(val creating: Boolean) : AdminPanelMutation
    data class SetCreateGroupError(val error: StringKey?) : AdminPanelMutation
    data object SetCreateGroupSuccess : AdminPanelMutation
    // Invite member dialog mutations
    data object ShowInviteDialog : AdminPanelMutation
    data object HideInviteDialog : AdminPanelMutation
    data class SetInviteEmail(val email: String) : AdminPanelMutation
    data class SetSendingInvite(val sending: Boolean) : AdminPanelMutation
    data class SetInviteError(val error: StringKey?) : AdminPanelMutation
    data object SetInviteSuccess : AdminPanelMutation
    // Pending invitations mutations
    data class SetInvitations(val invitations: List<InvitationResponse>) : AdminPanelMutation
    data class SetLoadingInvitations(val loading: Boolean) : AdminPanelMutation
    data class ShowRevokeDialog(val invitation: InvitationResponse) : AdminPanelMutation
    data object HideRevokeDialog : AdminPanelMutation
    data class SetRevoking(val revoking: Boolean) : AdminPanelMutation
    data class SetResending(val resending: Boolean) : AdminPanelMutation
    // Remove member mutations
    data class ShowRemoveMemberDialog(val member: MemberResponse) : AdminPanelMutation
    data object HideRemoveMemberDialog : AdminPanelMutation
    data class SetRemovingMember(val removing: Boolean) : AdminPanelMutation
    data class RemoveMemberFromList(val userId: String) : AdminPanelMutation
}
