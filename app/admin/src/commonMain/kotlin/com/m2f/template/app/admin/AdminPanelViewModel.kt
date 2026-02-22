package com.m2f.template.app.admin

import androidx.lifecycle.viewModelScope
import com.m2f.template.core.mvi.MviViewModel
import com.m2f.template.models.dto.CreateGroupRequest
import com.m2f.template.models.localization.StringKey
import com.m2f.template.sdk.Sdk
import kotlinx.coroutines.launch

class AdminPanelViewModel(
    private val sdk: Sdk,
) : MviViewModel<AdminPanelIntent, AdminPanelModel, AdminPanelMutation, AdminPanelEvent>(
    initialState = AdminPanelModel(),
) {

    override fun take(intent: AdminPanelIntent) {
        viewModelScope.launch {
            when (intent) {
                is AdminPanelIntent.LoadAdminPanel -> handleLoadAdminPanel(intent.groupId)
                is AdminPanelIntent.LoadMoreMembers -> handleLoadMoreMembers()
                is AdminPanelIntent.RegisterMemberClicked -> {
                    val groupId = model.value.groupId
                    if (groupId.isNotBlank()) {
                        sendEvent(AdminPanelEvent.NavigateToRegisterMember(groupId))
                    }
                }
                is AdminPanelIntent.OpenCreateGroupDialog -> sendMutation(AdminPanelMutation.ShowCreateGroupDialog)
                is AdminPanelIntent.CloseCreateGroupDialog -> sendMutation(AdminPanelMutation.HideCreateGroupDialog)
                is AdminPanelIntent.CreateGroupNameChanged -> sendMutation(AdminPanelMutation.SetCreateGroupName(intent.name))
                is AdminPanelIntent.SubmitCreateGroup -> handleCreateGroup()
            }
        }
    }

    private suspend fun handleLoadAdminPanel(groupId: String) {
        sendMutation(AdminPanelMutation.SetLoading(true))
        sdk.getGroup(groupId).fold(
            ifLeft = { error ->
                sendMutation(AdminPanelMutation.SetError(StringKey.fromCode(error.code) ?: StringKey.GENERIC_ERROR))
                sendMutation(AdminPanelMutation.SetLoading(false))
            },
            ifRight = { group ->
                sendMutation(
                    AdminPanelMutation.SetGroupInfo(
                        groupId = group.id,
                        groupName = group.name,
                        groupSlug = group.slug,
                        groupDescription = group.description,
                        memberCount = group.memberCount,
                    ),
                )
                sdk.getMembers(groupId, cursor = null, limit = 20).fold(
                    ifLeft = { error ->
                        sendMutation(AdminPanelMutation.SetError(StringKey.fromCode(error.code) ?: StringKey.GENERIC_ERROR))
                        sendMutation(AdminPanelMutation.SetLoading(false))
                    },
                    ifRight = { page ->
                        sendMutation(
                            AdminPanelMutation.SetMembers(
                                members = page.items,
                                cursor = page.cursor,
                                hasMore = page.hasMore,
                            ),
                        )
                        sendMutation(AdminPanelMutation.SetLoading(false))
                    },
                )
            },
        )
    }

    private suspend fun handleLoadMoreMembers() {
        val current = model.value
        if (current.isLoadingMore || !current.hasMoreMembers || current.membersCursor == null) return
        sendMutation(AdminPanelMutation.SetLoadingMore(true))
        sdk.getMembers(current.groupId, cursor = current.membersCursor, limit = 20).fold(
            ifLeft = { error ->
                sendMutation(AdminPanelMutation.SetError(StringKey.fromCode(error.code) ?: StringKey.GENERIC_ERROR))
                sendMutation(AdminPanelMutation.SetLoadingMore(false))
            },
            ifRight = { page ->
                sendMutation(
                    AdminPanelMutation.AppendMembers(
                        members = page.items,
                        cursor = page.cursor,
                        hasMore = page.hasMore,
                    ),
                )
                sendMutation(AdminPanelMutation.SetLoadingMore(false))
            },
        )
    }

    private suspend fun handleCreateGroup() {
        val current = model.value
        val name = current.createGroupName.trim()

        if (name.isBlank()) {
            sendMutation(AdminPanelMutation.SetCreateGroupError(StringKey.VALIDATION_NAME_BLANK))
            return
        }

        sendMutation(AdminPanelMutation.SetCreatingGroup(true))
        sendMutation(AdminPanelMutation.SetCreateGroupError(null))

        // Generate slug from name: lowercase, replace spaces with hyphens
        val slug = name.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')

        sdk.createGroup(CreateGroupRequest(name = name, slug = slug)).fold(
            ifLeft = { error ->
                sendMutation(AdminPanelMutation.SetCreateGroupError(StringKey.fromCode(error.code) ?: StringKey.GENERIC_ERROR))
                sendMutation(AdminPanelMutation.SetCreatingGroup(false))
            },
            ifRight = { group ->
                sendMutation(AdminPanelMutation.SetCreatingGroup(false))
                sendMutation(AdminPanelMutation.SetCreateGroupSuccess)
                sendEvent(AdminPanelEvent.GroupCreated(group.id, group.slug))
            },
        )
    }

    override suspend fun reduce(
        model: AdminPanelModel,
        mutation: AdminPanelMutation,
    ): AdminPanelModel = when (mutation) {
        is AdminPanelMutation.SetLoading -> model.copy(isLoading = mutation.loading)
        is AdminPanelMutation.SetLoadingMore -> model.copy(isLoadingMore = mutation.loading)
        is AdminPanelMutation.SetGroupInfo -> model.copy(
            groupId = mutation.groupId,
            groupName = mutation.groupName,
            groupSlug = mutation.groupSlug,
            groupDescription = mutation.groupDescription,
            memberCount = mutation.memberCount,
        )
        is AdminPanelMutation.SetMembers -> model.copy(
            members = mutation.members,
            membersCursor = mutation.cursor,
            hasMoreMembers = mutation.hasMore,
        )
        is AdminPanelMutation.AppendMembers -> model.copy(
            members = model.members + mutation.members,
            membersCursor = mutation.cursor,
            hasMoreMembers = mutation.hasMore,
        )
        is AdminPanelMutation.SetError -> model.copy(error = mutation.error)
        is AdminPanelMutation.ShowCreateGroupDialog -> model.copy(
            showCreateGroupDialog = true,
            createGroupName = "",
            createGroupError = null,
            createGroupSuccess = false,
        )
        is AdminPanelMutation.HideCreateGroupDialog -> model.copy(
            showCreateGroupDialog = false,
            createGroupName = "",
            createGroupError = null,
        )
        is AdminPanelMutation.SetCreateGroupName -> model.copy(createGroupName = mutation.name)
        is AdminPanelMutation.SetCreatingGroup -> model.copy(isCreatingGroup = mutation.creating)
        is AdminPanelMutation.SetCreateGroupError -> model.copy(createGroupError = mutation.error)
        is AdminPanelMutation.SetCreateGroupSuccess -> model.copy(
            showCreateGroupDialog = false,
            createGroupSuccess = true,
        )
    }
}
