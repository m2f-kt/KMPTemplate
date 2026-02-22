package com.m2f.template.app.admin

sealed interface AdminPanelIntent {
    data class LoadAdminPanel(val groupId: String) : AdminPanelIntent
    data object LoadMoreMembers : AdminPanelIntent
    data object RegisterMemberClicked : AdminPanelIntent
    data object OpenCreateGroupDialog : AdminPanelIntent
    data object CloseCreateGroupDialog : AdminPanelIntent
    data class CreateGroupNameChanged(val name: String) : AdminPanelIntent
    data object SubmitCreateGroup : AdminPanelIntent
}
