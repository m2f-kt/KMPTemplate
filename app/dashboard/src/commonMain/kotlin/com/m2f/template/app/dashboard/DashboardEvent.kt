package com.m2f.template.app.dashboard

sealed interface DashboardEvent {
    data object NavigateToLogin : DashboardEvent
}
