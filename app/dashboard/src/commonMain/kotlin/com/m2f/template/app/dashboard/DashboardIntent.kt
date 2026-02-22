package com.m2f.template.app.dashboard

sealed interface DashboardIntent {
    data object LoadDashboard : DashboardIntent
    data object RefreshProfile : DashboardIntent
    data class NavItemSelected(val item: String) : DashboardIntent
    data object LogoutClicked : DashboardIntent
    data object AdminPanelClicked : DashboardIntent
}
