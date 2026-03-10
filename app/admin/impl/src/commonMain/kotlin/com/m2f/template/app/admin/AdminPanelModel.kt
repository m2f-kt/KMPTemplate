package com.m2f.template.app.admin

import com.m2f.template.models.dto.InvitationResponse
import com.m2f.template.models.dto.MemberResponse
import com.m2f.template.models.localization.StringKey

data class AdminPanelModel(
    val groupId: String = "",
    val groupName: String = "",
    val groupSlug: String = "",
    val groupDescription: String = "",
    val memberCount: Int = 0,
    val members: List<MemberResponse> = emptyList(),
    val membersCursor: String? = null,
    val hasMoreMembers: Boolean = false,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: StringKey? = null,
    // Create group dialog state
    val showCreateGroupDialog: Boolean = false,
    val createGroupName: String = "",
    val isCreatingGroup: Boolean = false,
    val createGroupError: StringKey? = null,
    val createGroupSuccess: Boolean = false,
    // Invite member dialog state
    val showInviteDialog: Boolean = false,
    val inviteEmail: String = "",
    val isSendingInvite: Boolean = false,
    val inviteError: StringKey? = null,
    val inviteSuccess: Boolean = false,
    // Pending invitations section state
    val invitations: List<InvitationResponse> = emptyList(),
    val isLoadingInvitations: Boolean = false,
    val showRevokeDialog: Boolean = false,
    val revokeTarget: InvitationResponse? = null,
    val isRevoking: Boolean = false,
    val isResending: Boolean = false,
    // Remove member dialog state
    val showRemoveMemberDialog: Boolean = false,
    val removeMemberTarget: MemberResponse? = null,
    val isRemovingMember: Boolean = false,
)
