package com.m2f.template.app.profile

import app.cash.turbine.turbineScope
import arrow.core.Either
import com.m2f.template.core.testing.fakes.fakeSdk
import com.m2f.template.models.AppError
import com.m2f.template.models.UserRole
import com.m2f.template.models.UserTier
import com.m2f.template.models.dto.UserResponse
import io.kotest.matchers.shouldBe
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val testUser = UserResponse(
        id = "u1",
        email = "a@b.com",
        name = "Test User",
        role = UserRole.User,
    )

    @Test
    fun `load profile populates model with user data`() = runTest(testDispatcher) {
        val sdk = fakeSdk {
            user { getProfile { Either.Right(testUser) } }
        }
        val viewModel = ProfileViewModel(sdk)
        advanceUntilIdle()

        // With SharingStarted.Eagerly + init eager model access,
        // the model already has the loaded state
        viewModel.model.value shouldBe ProfileModel(
            userId = "u1",
            email = "a@b.com",
            name = "Test User",
            tier = UserTier.Free,
            isLoading = false,
        )
    }

    @Test
    fun `save profile shows success in model`() = runTest(testDispatcher) {
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
        advanceUntilIdle()

        turbineScope {
            val modelTurbine = viewModel.model.testIn(backgroundScope)

            // Current state: loaded
            modelTurbine.awaitItem() shouldBe ProfileModel(
                userId = "u1",
                email = "a@b.com",
                name = "Test User",
                tier = UserTier.Free,
                isLoading = false,
            )

            viewModel.take(ProfileIntent.StartEditing)
            advanceUntilIdle()
            modelTurbine.awaitItem() shouldBe ProfileModel(
                userId = "u1",
                email = "a@b.com",
                name = "Test User",
                tier = UserTier.Free,
                isLoading = false,
                isEditing = true,
                editName = "Test User",
                editEmail = "a@b.com",
            )

            viewModel.take(ProfileIntent.EditNameChanged("New Name"))
            advanceUntilIdle()
            modelTurbine.awaitItem() shouldBe ProfileModel(
                userId = "u1",
                email = "a@b.com",
                name = "Test User",
                tier = UserTier.Free,
                isLoading = false,
                isEditing = true,
                editName = "New Name",
                editEmail = "a@b.com",
            )

            viewModel.take(ProfileIntent.EditEmailChanged("new@b.com"))
            advanceUntilIdle()
            modelTurbine.awaitItem() shouldBe ProfileModel(
                userId = "u1",
                email = "a@b.com",
                name = "Test User",
                tier = UserTier.Free,
                isLoading = false,
                isEditing = true,
                editName = "New Name",
                editEmail = "new@b.com",
            )

            viewModel.take(ProfileIntent.SaveProfileClicked)
            advanceUntilIdle()
            // StateFlow conflation: SetLoading + SetProfile + SetSaveSuccess conflate
            modelTurbine.awaitItem() shouldBe ProfileModel(
                userId = "u1",
                email = "new@b.com",
                name = "New Name",
                tier = UserTier.Free,
                isLoading = false,
                isEditing = false,
                editName = "New Name",
                editEmail = "new@b.com",
                saveSuccess = true,
            )

            modelTurbine.cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `load profile failure shows server error`() = runTest(testDispatcher) {
        val sdk = fakeSdk {
            user { getProfile { Either.Left(AppError.Server.Internal(message = "Network error")) } }
        }
        val viewModel = ProfileViewModel(sdk)
        advanceUntilIdle()

        // With eager init, the model already has the error state
        viewModel.model.value shouldBe ProfileModel(
            isLoading = false,
            serverError = "Network error",
        )
    }

    @Test
    fun `logout emits NavigateToLogin event`() = runTest(testDispatcher) {
        val sdk = fakeSdk {
            auth { logout { Either.Right(Unit) } }
            user { getProfile { Either.Right(testUser) } }
        }
        val viewModel = ProfileViewModel(sdk)
        advanceUntilIdle()

        turbineScope {
            val eventTurbine = viewModel.event.testIn(backgroundScope)

            viewModel.take(ProfileIntent.LogoutClicked)
            advanceUntilIdle()
            eventTurbine.awaitItem() shouldBe ProfileEvent.NavigateToLogin

            eventTurbine.cancelAndIgnoreRemainingEvents()
        }
    }
}
