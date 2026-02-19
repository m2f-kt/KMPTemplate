package com.m2f.template.app.admin

import com.m2f.template.models.dto.MemberResponse

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
    val error: String? = null,
)
