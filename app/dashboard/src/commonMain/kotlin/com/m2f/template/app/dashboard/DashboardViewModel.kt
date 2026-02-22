package com.m2f.template.app.dashboard

import androidx.lifecycle.viewModelScope
import com.m2f.template.core.mvi.MviViewModel
import com.m2f.template.models.GroupRole
import com.m2f.template.models.UserRole
import com.m2f.template.sdk.Sdk
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val sdk: Sdk,
) : MviViewModel<DashboardIntent, DashboardModel, DashboardMutation, DashboardEvent>(
    initialState = DashboardModel()
) {

    init {
        take(DashboardIntent.LoadDashboard)
    }

    override fun take(intent: DashboardIntent) {
        viewModelScope.launch {
            when (intent) {
                is DashboardIntent.LoadDashboard -> {
                    sendMutation(DashboardMutation.SetLoading(true))
                    delay(300)
                    // Check system-level role
                    sdk.getProfile().onRight { user ->
                        val isSystemAdmin = user.role.level >= UserRole.Admin.level
                        sendMutation(DashboardMutation.SetSystemAdmin(isSystemAdmin))
                        sendMutation(DashboardMutation.SetUserName(user.name.ifBlank { user.email }))
                        sendMutation(DashboardMutation.SetAvatarUrl(user.avatarUrl))
                    }
                    // Load memberships for role-gated nav
                    sdk.getMyMemberships().fold(
                        ifLeft = { /* Silently ignore — user may not be in any group */ },
                        ifRight = { memberships ->
                            val adminMembership = memberships.firstOrNull { membership ->
                                membership.groupRole.level >= GroupRole.Admin.level
                            }
                            sendMutation(
                                DashboardMutation.SetMembership(
                                    isAdmin = adminMembership != null,
                                    groupId = adminMembership?.groupId,
                                    groupName = adminMembership?.groupName,
                                ),
                            )
                        },
                    )
                    sendMutation(DashboardMutation.SetLoading(false))
                }
                is DashboardIntent.NavItemSelected -> {
                    sendMutation(DashboardMutation.SetNavItem(intent.item))
                }
                is DashboardIntent.RefreshProfile -> {
                    // Lightweight refresh of just user profile data (name + avatar)
                    sdk.getProfile().onRight { user ->
                        sendMutation(DashboardMutation.SetUserName(user.name.ifBlank { user.email }))
                        sendMutation(DashboardMutation.SetAvatarUrl(user.avatarUrl))
                    }
                }
                is DashboardIntent.LogoutClicked -> {
                    sdk.logout()
                    sendEvent(DashboardEvent.NavigateToLogin)
                }
                is DashboardIntent.AdminPanelClicked -> {
                    sendEvent(DashboardEvent.NavigateToAdmin(model.value.groupId))
                }
            }
        }
    }

    override suspend fun reduce(model: DashboardModel, mutation: DashboardMutation): DashboardModel =
        when (mutation) {
            is DashboardMutation.SetLoading -> model.copy(isLoading = mutation.loading)
            is DashboardMutation.SetNavItem -> model.copy(selectedNavItem = mutation.item)
            is DashboardMutation.SetMembership -> model.copy(
                isAdmin = mutation.isAdmin || model.isSystemAdmin,
                groupId = mutation.groupId,
                groupName = mutation.groupName,
            )
            is DashboardMutation.SetSystemAdmin -> model.copy(
                isSystemAdmin = mutation.isSystemAdmin,
                isAdmin = mutation.isSystemAdmin || model.isAdmin,
            )
            is DashboardMutation.SetUserName -> model.copy(userName = mutation.userName)
            is DashboardMutation.SetAvatarUrl -> model.copy(avatarUrl = mutation.avatarUrl)
        }
}
