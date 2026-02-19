package com.m2f.template.app.admin

sealed interface AdminPanelIntent {
    data class LoadAdminPanel(val groupId: String) : AdminPanelIntent
    data object LoadMoreMembers : AdminPanelIntent
    data object RegisterMemberClicked : AdminPanelIntent
}
