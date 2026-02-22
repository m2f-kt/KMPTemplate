package com.m2f.template.app.admin

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
}
