package com.m2f.template.app.dashboard

import arrow.core.Either
import com.m2f.template.core.testing.ViewModelTest
import com.m2f.template.core.testing.fakes.fakeSdk
import com.m2f.template.core.testing.test
import com.m2f.template.models.GroupRole
import com.m2f.template.models.UserRole
import com.m2f.template.models.dto.MembershipSummary
import com.m2f.template.models.dto.UserResponse
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
            // Process init's LoadDashboard side effects first (init fires on the
            // pre-runTest dispatcher; this drains the queued mutations so the
            // event-only assertion below isn't racing the loading state).
            intent(DashboardIntent.LoadDashboard)
            model(DashboardModel(isLoading = true))
            model(DashboardModel(isLoading = false))
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

    @Test
    fun `LoadDashboard with system admin role sets isAdmin true even without group memberships`() {
        val sdk = fakeSdk {
            user {
                getProfile {
                    Either.Right(
                        UserResponse(
                            id = "1",
                            email = "admin@test.com",
                            name = "Admin",
                            role = UserRole.Admin,
                        ),
                    )
                }
            }
        }
        val viewModel = DashboardViewModel(sdk)
        viewModel.test {
            intent(DashboardIntent.LoadDashboard)
            model(DashboardModel(isLoading = true))
            // getProfile returns admin → SetSystemAdmin fires, then SetLoading(false)
            // The final observed state after conflation includes isAdmin=true, isSystemAdmin=true
            model(
                DashboardModel(
                    isLoading = false,
                    isAdmin = true,
                    isSystemAdmin = true,
                    userName = "Admin",
                ),
            )
        }
    }

    @Test
    fun `AdminPanelClicked emits NavigateToAdmin with null groupId for system admin without groups`() {
        val sdk = fakeSdk {
            user {
                getProfile {
                    Either.Right(
                        UserResponse(
                            id = "1",
                            email = "admin@test.com",
                            name = "Admin",
                            role = UserRole.Admin,
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
                    isSystemAdmin = true,
                    userName = "Admin",
                ),
            )
            intent(DashboardIntent.AdminPanelClicked)
            event(DashboardEvent.NavigateToAdmin(null))
        }
    }

    @Test
    fun `LoadDashboard with system admin AND group admin membership gets groupId`() {
        val sdk = fakeSdk {
            user {
                getProfile {
                    Either.Right(
                        UserResponse(
                            id = "1",
                            email = "admin@test.com",
                            name = "Admin",
                            role = UserRole.Admin,
                        ),
                    )
                }
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
            // Both getProfile (admin) and getMyMemberships (admin group) succeed
            // Final state: isAdmin=true, isSystemAdmin=true, groupId="group-1"
            model(
                DashboardModel(
                    isLoading = false,
                    isAdmin = true,
                    isSystemAdmin = true,
                    userName = "Admin",
                    groupId = "group-1",
                    groupName = "Test Group",
                ),
            )
        }
    }
}
