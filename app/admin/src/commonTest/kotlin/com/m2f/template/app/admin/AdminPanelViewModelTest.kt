package com.m2f.template.app.admin

import arrow.core.Either
import com.m2f.template.core.testing.ViewModelTest
import com.m2f.template.core.testing.fakes.fakeSdk
import com.m2f.template.core.testing.test
import com.m2f.template.models.AppError
import com.m2f.template.models.GroupRole
import com.m2f.template.models.dto.GroupResponse
import com.m2f.template.models.dto.MemberResponse
import com.m2f.template.models.dto.PaginatedMemberResponse
import com.m2f.template.models.localization.StringKey
import kotlin.test.Test

class AdminPanelViewModelTest : ViewModelTest() {

    private val testGroup = GroupResponse(
        id = "g1",
        name = "Test Group",
        slug = "test-group",
        description = "A test group",
        createdBy = "u1",
        memberCount = 2,
        createdAt = "2026-01-01",
        updatedAt = "2026-01-01",
    )

    private val testMembers = PaginatedMemberResponse(
        items = listOf(
            MemberResponse(userId = "u1", email = "admin@test.com", name = "Admin User", role = GroupRole.Admin, joinedAt = "2026-01-01"),
            MemberResponse(userId = "u2", email = "member@test.com", name = "Member User", role = GroupRole.Member, joinedAt = "2026-01-02"),
        ),
        cursor = null,
        hasMore = false,
    )

    @Test
    fun `LoadAdminPanel loads group info and members`() {
        val sdk = fakeSdk {
            group {
                getGroup { Either.Right(testGroup) }
                getMembers { _, _, _ -> Either.Right(testMembers) }
            }
        }
        val viewModel = AdminPanelViewModel(sdk)
        viewModel.test {
            intent(AdminPanelIntent.LoadAdminPanel("g1"))
            // Sync fakes conflate all intermediate states; assert final settled state only
            model(
                AdminPanelModel(
                    isLoading = false,
                    groupId = "g1",
                    groupName = "Test Group",
                    groupSlug = "test-group",
                    groupDescription = "A test group",
                    memberCount = 2,
                    members = testMembers.items,
                    membersCursor = null,
                    hasMoreMembers = false,
                ),
            )
        }
    }

    @Test
    fun `LoadAdminPanel with getGroup error shows error`() {
        val sdk = fakeSdk {
            group {
                getGroup { Either.Left(AppError.Group.Forbidden(message = "Access denied")) }
            }
        }
        val viewModel = AdminPanelViewModel(sdk)
        viewModel.test {
            intent(AdminPanelIntent.LoadAdminPanel("g1"))
            // Sync fakes conflate SetLoading(true) + SetError + SetLoading(false); assert final state
            model(AdminPanelModel(isLoading = false, error = StringKey.GROUP_FORBIDDEN))
        }
    }

    @Test
    fun `RegisterMemberClicked emits NavigateToRegisterMember`() {
        val sdk = fakeSdk {
            group {
                getGroup { Either.Right(testGroup) }
                getMembers { _, _, _ -> Either.Right(testMembers) }
            }
        }
        val viewModel = AdminPanelViewModel(sdk)
        viewModel.test {
            intent(AdminPanelIntent.LoadAdminPanel("g1"))
            model(
                AdminPanelModel(
                    isLoading = false,
                    groupId = "g1",
                    groupName = "Test Group",
                    groupSlug = "test-group",
                    groupDescription = "A test group",
                    memberCount = 2,
                    members = testMembers.items,
                    membersCursor = null,
                    hasMoreMembers = false,
                ),
            )
            intent(AdminPanelIntent.RegisterMemberClicked)
            event(AdminPanelEvent.NavigateToRegisterMember("g1"))
        }
    }

    @Test
    fun `ExecuteRemoveMember success removes member from list`() {
        val sdk = fakeSdk {
            group {
                getGroup { Either.Right(testGroup) }
                getMembers { _, _, _ -> Either.Right(testMembers) }
                removeMember { _, _ -> Either.Right(Unit) }
            }
        }
        val viewModel = AdminPanelViewModel(sdk)
        viewModel.test {
            intent(AdminPanelIntent.LoadAdminPanel("g1"))
            model(
                AdminPanelModel(
                    isLoading = false,
                    groupId = "g1",
                    groupName = "Test Group",
                    groupSlug = "test-group",
                    groupDescription = "A test group",
                    memberCount = 2,
                    members = testMembers.items,
                    membersCursor = null,
                    hasMoreMembers = false,
                ),
            )
            intent(AdminPanelIntent.ConfirmRemoveMember(testMembers.items[1]))
            model(
                AdminPanelModel(
                    isLoading = false,
                    groupId = "g1",
                    groupName = "Test Group",
                    groupSlug = "test-group",
                    groupDescription = "A test group",
                    memberCount = 2,
                    members = testMembers.items,
                    membersCursor = null,
                    hasMoreMembers = false,
                    showRemoveMemberDialog = true,
                    removeMemberTarget = testMembers.items[1],
                ),
            )
            intent(AdminPanelIntent.ExecuteRemoveMember)
            model(
                AdminPanelModel(
                    isLoading = false,
                    groupId = "g1",
                    groupName = "Test Group",
                    groupSlug = "test-group",
                    groupDescription = "A test group",
                    memberCount = 1,
                    members = listOf(testMembers.items[0]),
                    membersCursor = null,
                    hasMoreMembers = false,
                    showRemoveMemberDialog = false,
                    removeMemberTarget = null,
                    isRemovingMember = false,
                ),
            )
        }
    }

    @Test
    fun `ExecuteRemoveMember failure closes dialog and sets error`() {
        val sdk = fakeSdk {
            group {
                getGroup { Either.Right(testGroup) }
                getMembers { _, _, _ -> Either.Right(testMembers) }
                removeMember { _, _ -> Either.Left(AppError.Group.Forbidden(message = "Cannot remove owner")) }
            }
        }
        val viewModel = AdminPanelViewModel(sdk)
        viewModel.test {
            intent(AdminPanelIntent.LoadAdminPanel("g1"))
            model(
                AdminPanelModel(
                    isLoading = false,
                    groupId = "g1",
                    groupName = "Test Group",
                    groupSlug = "test-group",
                    groupDescription = "A test group",
                    memberCount = 2,
                    members = testMembers.items,
                    membersCursor = null,
                    hasMoreMembers = false,
                ),
            )
            intent(AdminPanelIntent.ConfirmRemoveMember(testMembers.items[1]))
            model(
                AdminPanelModel(
                    isLoading = false,
                    groupId = "g1",
                    groupName = "Test Group",
                    groupSlug = "test-group",
                    groupDescription = "A test group",
                    memberCount = 2,
                    members = testMembers.items,
                    membersCursor = null,
                    hasMoreMembers = false,
                    showRemoveMemberDialog = true,
                    removeMemberTarget = testMembers.items[1],
                ),
            )
            intent(AdminPanelIntent.ExecuteRemoveMember)
            model(
                AdminPanelModel(
                    isLoading = false,
                    groupId = "g1",
                    groupName = "Test Group",
                    groupSlug = "test-group",
                    groupDescription = "A test group",
                    memberCount = 2,
                    members = testMembers.items,
                    membersCursor = null,
                    hasMoreMembers = false,
                    showRemoveMemberDialog = false,
                    removeMemberTarget = null,
                    isRemovingMember = false,
                    error = StringKey.GROUP_FORBIDDEN,
                ),
            )
        }
    }

    @Test
    fun `CancelRemoveMember hides dialog`() {
        val sdk = fakeSdk {
            group {
                getGroup { Either.Right(testGroup) }
                getMembers { _, _, _ -> Either.Right(testMembers) }
            }
        }
        val viewModel = AdminPanelViewModel(sdk)
        viewModel.test {
            intent(AdminPanelIntent.LoadAdminPanel("g1"))
            model(
                AdminPanelModel(
                    isLoading = false,
                    groupId = "g1",
                    groupName = "Test Group",
                    groupSlug = "test-group",
                    groupDescription = "A test group",
                    memberCount = 2,
                    members = testMembers.items,
                    membersCursor = null,
                    hasMoreMembers = false,
                ),
            )
            intent(AdminPanelIntent.ConfirmRemoveMember(testMembers.items[1]))
            model(
                AdminPanelModel(
                    isLoading = false,
                    groupId = "g1",
                    groupName = "Test Group",
                    groupSlug = "test-group",
                    groupDescription = "A test group",
                    memberCount = 2,
                    members = testMembers.items,
                    membersCursor = null,
                    hasMoreMembers = false,
                    showRemoveMemberDialog = true,
                    removeMemberTarget = testMembers.items[1],
                ),
            )
            intent(AdminPanelIntent.CancelRemoveMember)
            model(
                AdminPanelModel(
                    isLoading = false,
                    groupId = "g1",
                    groupName = "Test Group",
                    groupSlug = "test-group",
                    groupDescription = "A test group",
                    memberCount = 2,
                    members = testMembers.items,
                    membersCursor = null,
                    hasMoreMembers = false,
                    showRemoveMemberDialog = false,
                    removeMemberTarget = null,
                ),
            )
        }
    }

    @Test
    fun `LoadMoreMembers appends to member list`() {
        val firstPage = PaginatedMemberResponse(
            items = listOf(testMembers.items.first()),
            cursor = "cursor-1",
            hasMore = true,
        )
        val secondPage = PaginatedMemberResponse(
            items = listOf(testMembers.items.last()),
            cursor = null,
            hasMore = false,
        )
        var callCount = 0
        val sdk = fakeSdk {
            group {
                getGroup { Either.Right(testGroup) }
                getMembers { _, _, _ ->
                    callCount++
                    if (callCount == 1) Either.Right(firstPage) else Either.Right(secondPage)
                }
            }
        }
        val viewModel = AdminPanelViewModel(sdk)
        viewModel.test {
            intent(AdminPanelIntent.LoadAdminPanel("g1"))
            // Sync fakes conflate all mutations; assert final settled state
            model(
                AdminPanelModel(
                    isLoading = false,
                    groupId = "g1",
                    groupName = "Test Group",
                    groupSlug = "test-group",
                    groupDescription = "A test group",
                    memberCount = 2,
                    members = firstPage.items,
                    membersCursor = "cursor-1",
                    hasMoreMembers = true,
                ),
            )
            intent(AdminPanelIntent.LoadMoreMembers)
            model(
                AdminPanelModel(
                    isLoading = false,
                    groupId = "g1",
                    groupName = "Test Group",
                    groupSlug = "test-group",
                    groupDescription = "A test group",
                    memberCount = 2,
                    members = testMembers.items,
                    membersCursor = null,
                    hasMoreMembers = false,
                    isLoadingMore = false,
                ),
            )
        }
    }
}
