package com.m2f.template.app.dashboard

data class DashboardModel(
    val metrics: List<DashboardMockData.MetricItem> = DashboardMockData.metrics,
    val processes: List<DashboardMockData.ProcessItem> = DashboardMockData.processes,
    val activities: List<DashboardMockData.ActivityItem> = DashboardMockData.activities,
    val deployment: DashboardMockData.DeploymentStatus = DashboardMockData.deployment,
    val isLoading: Boolean = false,
    val userName: String = "user@terminal.dev",
    val selectedNavItem: String = "dashboard",
    val isAdmin: Boolean = false,
    val groupId: String? = null,
    val groupName: String? = null,
)
