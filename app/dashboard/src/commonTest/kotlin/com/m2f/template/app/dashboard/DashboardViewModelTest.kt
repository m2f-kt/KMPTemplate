package com.m2f.template.app.dashboard

import com.m2f.template.core.testing.ViewModelTest
import com.m2f.template.core.testing.fakes.fakeSdk
import com.m2f.template.core.testing.test
import kotlin.test.Test

class DashboardViewModelTest : ViewModelTest() {

    @Test
    fun `LoadDashboard toggles loading state`() {
        val sdk = fakeSdk()
        val viewModel = DashboardViewModel(sdk)
        viewModel.test {
            intent(DashboardIntent.LoadDashboard)
            model(DashboardModel(isLoading = true))
            model(DashboardModel(isLoading = false))
        }
    }

    @Test
    fun `NavItemSelected updates selectedNavItem`() {
        val sdk = fakeSdk()
        val viewModel = DashboardViewModel(sdk)
        viewModel.test {
            // Fire LoadDashboard explicitly (init's launch is on orphaned dispatcher)
            intent(DashboardIntent.LoadDashboard)
            model(DashboardModel(isLoading = true))
            model(DashboardModel(isLoading = false))
            // Now dispatch NavItemSelected
            intent(DashboardIntent.NavItemSelected("processes"))
            model(DashboardModel(selectedNavItem = "processes"))
        }
    }
}
