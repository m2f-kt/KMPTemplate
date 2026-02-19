package com.m2f.template.app.admin

sealed interface AdminPanelEvent {
    data class NavigateToRegisterMember(val groupId: String) : AdminPanelEvent
}
