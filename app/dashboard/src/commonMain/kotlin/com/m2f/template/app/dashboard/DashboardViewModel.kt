package com.m2f.template.app.dashboard

import androidx.lifecycle.viewModelScope
import com.m2f.template.core.mvi.MviViewModel
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
                }
                is DashboardIntent.NavItemSelected -> {
                    sendMutation(DashboardMutation.SetNavItem(intent.item))
                }
                is DashboardIntent.LogoutClicked -> {
                    sdk.logout()
                    sendEvent(DashboardEvent.NavigateToLogin)
                }
            }
        }
    }

    override suspend fun reduce(model: DashboardModel, mutation: DashboardMutation): DashboardModel =
        when (mutation) {
            is DashboardMutation.SetLoading -> model.copy(isLoading = mutation.loading)
            is DashboardMutation.SetNavItem -> model.copy(selectedNavItem = mutation.item)
        }
}
