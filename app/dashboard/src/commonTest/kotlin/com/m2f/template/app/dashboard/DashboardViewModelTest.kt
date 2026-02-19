package com.m2f.template.app.dashboard

import arrow.core.Either
import com.m2f.template.core.testing.ViewModelTest
import com.m2f.template.core.testing.fakes.fakeSdk
import com.m2f.template.core.testing.test
import com.m2f.template.models.GroupRole
import com.m2f.template.models.dto.MembershipSummary
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

    @Test
    fun `LogoutClicked emits NavigateToLogin event`() {
        val sdk = fakeSdk()
        val viewModel = DashboardViewModel(sdk)
        viewModel.test {
            intent(DashboardIntent.LogoutClicked)
            event(DashboardEvent.NavigateToLogin)
        }
    }

    @Test
    fun `LoadDashboard with admin membership sets isAdmin true`() {
        val sdk = fakeSdk {
            user {
                getMyMemberships {
                    Either.Right(
                        listOf(
                            MembershipSummary(
                                groupId = "group-1",
                                groupName = "Test Group",
                                groupRole = GroupRole.Admin,
                            ),
                        ),
                    )
                }
            }
        }
        val viewModel = DashboardViewModel(sdk)
        viewModel.test {
            intent(DashboardIntent.LoadDashboard)
            model(DashboardModel(isLoading = true))
            // StateFlow conflation: SetLoading(false) and SetMembership fire in quick succession
            // with sync fake, so the intermediate isLoading=false state may be conflated with
            // the membership update. The final observable state includes both.
            model(
                DashboardModel(
                    isLoading = false,
                    isAdmin = true,
                    groupId = "group-1",
                    groupName = "Test Group",
                ),
            )
        }
    }

    @Test
    fun `LoadDashboard with member-only membership does not set isAdmin`() {
        val sdk = fakeSdk {
            user {
                getMyMemberships {
                    Either.Right(
                        listOf(
                            MembershipSummary(
                                groupId = "group-1",
                                groupName = "Test Group",
                                groupRole = GroupRole.Member,
                            ),
                        ),
                    )
                }
            }
        }
        val viewModel = DashboardViewModel(sdk)
        viewModel.test {
            intent(DashboardIntent.LoadDashboard)
            model(DashboardModel(isLoading = true))
            // SetMembership fires with isAdmin=false, groupId=null, groupName=null
            // which doesn't change defaults — conflated with isLoading=false
            model(DashboardModel(isLoading = false))
        }
    }

    @Test
    fun `AdminPanelClicked emits NavigateToAdmin when groupId is set`() {
        val sdk = fakeSdk {
            user {
                getMyMemberships {
                    Either.Right(
                        listOf(
                            MembershipSummary(
                                groupId = "group-1",
                                groupName = "Test",
                                groupRole = GroupRole.Admin,
                            ),
                        ),
                    )
                }
            }
        }
        val viewModel = DashboardViewModel(sdk)
        viewModel.test {
            intent(DashboardIntent.LoadDashboard)
            model(DashboardModel(isLoading = true))
            model(
                DashboardModel(
                    isLoading = false,
                    isAdmin = true,
                    groupId = "group-1",
                    groupName = "Test",
                ),
            )
            intent(DashboardIntent.AdminPanelClicked)
            event(DashboardEvent.NavigateToAdmin("group-1"))
        }
    }
}
