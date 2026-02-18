package com.m2f.template.app.auth

import arrow.core.Either
import app.cash.turbine.test
import com.m2f.template.core.testing.ViewModelTest
import com.m2f.template.core.testing.fakes.fakeSdk
import com.m2f.template.models.AppError
import com.m2f.template.models.dto.AuthResponse
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest : ViewModelTest() {

    @Test
    fun `login success updates state with loginSuccess true`() = runTest {
        val sdk = fakeSdk {
            auth {
                login { _, _ ->
                    Either.Right(AuthResponse(accessToken = "test-access", refreshToken = "test-refresh", expiresIn = 3600))
                }
            }
        }
        val viewModel = LoginViewModel(sdk)

        viewModel.state.test {
            // Initial state
            awaitItem() shouldBe LoginState()

            // Enter email
            viewModel.onEmailChange("user@test.com")
            awaitItem().email shouldBe "user@test.com"

            // Enter password
            viewModel.onPasswordChange("password123")
            awaitItem().password shouldBe "password123"

            // Trigger login -- uses viewModelScope.launch internally
            viewModel.login()
            advanceUntilIdle() // drain viewModelScope coroutine

            // Loading state
            val loading = awaitItem()
            loading.isLoading shouldBe true

            // Success state
            val success = awaitItem()
            success.loginSuccess shouldBe true
            success.isLoading shouldBe false

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login with blank email shows email error`() = runTest {
        val sdk = fakeSdk() // unconfigured -- won't be called
        val viewModel = LoginViewModel(sdk)

        viewModel.state.test {
            awaitItem() // initial state

            // login() with blank email returns synchronously (no viewModelScope.launch)
            viewModel.login()
            val errorState = awaitItem()
            errorState.emailError shouldBe "Email must not be blank"

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login failure shows server error`() = runTest {
        val sdk = fakeSdk {
            auth {
                login { _, _ ->
                    Either.Left(AppError.Auth.InvalidCredentials())
                }
            }
        }
        val viewModel = LoginViewModel(sdk)

        viewModel.state.test {
            awaitItem() // initial state

            viewModel.onEmailChange("user@test.com")
            awaitItem()
            viewModel.onPasswordChange("password123")
            awaitItem()

            // Trigger login -- uses viewModelScope.launch internally
            viewModel.login()
            advanceUntilIdle() // drain viewModelScope coroutine

            val loading = awaitItem()
            loading.isLoading shouldBe true

            val error = awaitItem()
            error.serverError shouldBe "Email or password is incorrect"
            error.isLoading shouldBe false

            cancelAndIgnoreRemainingEvents()
        }
    }
}
