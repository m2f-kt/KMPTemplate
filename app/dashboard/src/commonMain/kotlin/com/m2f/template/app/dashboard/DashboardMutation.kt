package com.m2f.template.app.dashboard

sealed interface DashboardMutation {
    data class SetLoading(val loading: Boolean) : DashboardMutation
    data class SetNavItem(val item: String) : DashboardMutation
}
