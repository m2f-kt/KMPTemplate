package com.m2f.template.app.profile

import arrow.core.Either
import com.m2f.template.core.testing.ViewModelTest
import com.m2f.template.core.testing.fakes.fakeSdk
import com.m2f.template.core.testing.test
import com.m2f.template.models.AppError
import com.m2f.template.models.UserRole
import com.m2f.template.models.UserTier
import com.m2f.template.models.dto.UserResponse
import com.m2f.template.models.localization.StringKey
import kotlin.test.Test

class ProfileViewModelTest : ViewModelTest() {

    private val testUser = UserResponse(
        id = "u1",
        email = "a@b.com",
        name = "Test User",
        role = UserRole.User,
    )

    @Test
    fun `load profile populates model with user data`() {
        val sdk = fakeSdk {
            user { getProfile { Either.Right(testUser) } }
        }
        val viewModel = ProfileViewModel(sdk)
        viewModel.test {
            model(ProfileModel(userId = "u1", email = "a@b.com", name = "Test User", tier = UserTier.Free, isLoading = false))
        }
    }

    @Test
    fun `save profile shows success in model`() {
        val updatedUser = UserResponse(
            id = "u1",
            email = "new@b.com",
            name = "New Name",
            role = UserRole.User,
        )
        val sdk = fakeSdk {
            user {
                getProfile { Either.Right(testUser) }
                updateProfile { Either.Right(updatedUser) }
            }
        }
        val viewModel = ProfileViewModel(sdk)
        viewModel.test {
            model(ProfileModel(userId = "u1", email = "a@b.com", name = "Test User", tier = UserTier.Free, isLoading = false))
            intent(ProfileIntent.StartEditing)
            model(ProfileModel(userId = "u1", email = "a@b.com", name = "Test User", tier = UserTier.Free, isLoading = false, isEditing = true, editName = "Test User", editEmail = "a@b.com"))
            intent(ProfileIntent.EditNameChanged("New Name"))
            model(ProfileModel(userId = "u1", email = "a@b.com", name = "Test User", tier = UserTier.Free, isLoading = false, isEditing = true, editName = "New Name", editEmail = "a@b.com"))
            intent(ProfileIntent.EditEmailChanged("new@b.com"))
            model(ProfileModel(userId = "u1", email = "a@b.com", name = "Test User", tier = UserTier.Free, isLoading = false, isEditing = true, editName = "New Name", editEmail = "new@b.com"))
            intent(ProfileIntent.SaveProfileClicked)
            model(ProfileModel(userId = "u1", email = "new@b.com", name = "New Name", tier = UserTier.Free, isLoading = false, isEditing = false, editName = "New Name", editEmail = "new@b.com", saveSuccess = true))
        }
    }

    @Test
    fun `load profile failure shows server error`() {
        val sdk = fakeSdk {
            user { getProfile { Either.Left(AppError.Server.Internal(message = "Network error")) } }
        }
        val viewModel = ProfileViewModel(sdk)
        viewModel.test {
            model(ProfileModel(isLoading = false, serverError = StringKey.SERVER_INTERNAL_ERROR))
        }
    }

    @Test
    fun `logout emits NavigateToLogin event`() {
        val sdk = fakeSdk {
            auth { logout { Either.Right(Unit) } }
            user { getProfile { Either.Right(testUser) } }
        }
        val viewModel = ProfileViewModel(sdk)
        viewModel.test {
            model(ProfileModel(userId = "u1", email = "a@b.com", name = "Test User", tier = UserTier.Free, isLoading = false))
            intent(ProfileIntent.LogoutClicked)
            event(ProfileEvent.NavigateToLogin)
        }
    }
}
