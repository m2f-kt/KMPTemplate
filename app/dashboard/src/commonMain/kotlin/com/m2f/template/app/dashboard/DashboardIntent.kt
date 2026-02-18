package com.m2f.template.app.dashboard

sealed interface DashboardIntent {
    data object LoadDashboard : DashboardIntent
    data class NavItemSelected(val item: String) : DashboardIntent
}
