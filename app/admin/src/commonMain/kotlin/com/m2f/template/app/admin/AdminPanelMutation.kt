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
}
