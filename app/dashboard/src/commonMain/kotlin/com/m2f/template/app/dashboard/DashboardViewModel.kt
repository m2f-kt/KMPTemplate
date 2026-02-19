package com.m2f.template.app.dashboard

import androidx.lifecycle.viewModelScope
import com.m2f.template.core.mvi.MviViewModel
import com.m2f.template.models.GroupRole
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
                    sendMutation(DashboardMutation.SetLoading(false))
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
                }
                is DashboardIntent.NavItemSelected -> {
                    sendMutation(DashboardMutation.SetNavItem(intent.item))
                }
                is DashboardIntent.LogoutClicked -> {
                    sdk.logout()
                    sendEvent(DashboardEvent.NavigateToLogin)
                }
                is DashboardIntent.AdminPanelClicked -> {
                    val currentGroupId = model.value.groupId
                    if (currentGroupId != null) {
                        sendEvent(DashboardEvent.NavigateToAdmin(currentGroupId))
                    }
                }
            }
        }
    }

    override suspend fun reduce(model: DashboardModel, mutation: DashboardMutation): DashboardModel =
        when (mutation) {
            is DashboardMutation.SetLoading -> model.copy(isLoading = mutation.loading)
            is DashboardMutation.SetNavItem -> model.copy(selectedNavItem = mutation.item)
            is DashboardMutation.SetMembership -> model.copy(
                isAdmin = mutation.isAdmin,
                groupId = mutation.groupId,
                groupName = mutation.groupName,
            )
        }
}
