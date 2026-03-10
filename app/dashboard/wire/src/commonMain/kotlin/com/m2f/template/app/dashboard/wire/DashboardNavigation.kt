package com.m2f.template.app.dashboard.wire

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import com.m2f.template.app.admin.contract.AdminPanelRoute
import com.m2f.template.app.auth.contract.LoginRoute
import com.m2f.template.app.dashboard.DashboardEvent
import com.m2f.template.app.dashboard.DashboardIntent
import com.m2f.template.app.dashboard.DashboardScreen
import com.m2f.template.app.dashboard.DashboardViewModel
import com.m2f.template.app.dashboard.contract.DashboardRoute
import com.m2f.template.app.profile.contract.ProfileRoute
import com.m2f.template.navigation.Route
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<Route>.dashboardEntries(
    backStack: MutableList<Route>,
) {
    entry<DashboardRoute> {
        val viewModel = koinViewModel<DashboardViewModel>()
        val state by viewModel.model.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            viewModel.take(DashboardIntent.RefreshProfile)
        }

        DashboardScreen(
            state = state,
            onNavItemSelected = { viewModel.take(DashboardIntent.NavItemSelected(it)) },
            onProfileClick = { backStack.add(ProfileRoute) },
            onLogout = { viewModel.take(DashboardIntent.LogoutClicked) },
            onAdminClick = { viewModel.take(DashboardIntent.AdminPanelClicked) },
        )
        LaunchedEffect(Unit) {
            viewModel.event.collect { event ->
                when (event) {
                    is DashboardEvent.NavigateToLogin -> {
                        backStack.clear()
                        backStack.add(LoginRoute())
                    }
                    is DashboardEvent.NavigateToAdmin -> {
                        backStack.add(AdminPanelRoute(groupId = event.groupId))
                    }
                }
            }
        }
    }
}
