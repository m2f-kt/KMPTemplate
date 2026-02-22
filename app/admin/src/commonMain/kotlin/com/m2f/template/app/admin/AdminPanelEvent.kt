package com.m2f.template.app.admin

sealed interface AdminPanelEvent {
    data class NavigateToRegisterMember(val groupId: String) : AdminPanelEvent
    data class GroupCreated(val groupId: String, val groupSlug: String) : AdminPanelEvent
}
