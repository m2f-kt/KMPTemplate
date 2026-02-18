package com.m2f.template.app.auth

import arrow.core.Either
import com.m2f.template.core.testing.ViewModelTest
import com.m2f.template.core.testing.fakes.fakeSdk
import com.m2f.template.core.testing.test
import com.m2f.template.models.AppError
import com.m2f.template.models.dto.AuthResponse
import kotlin.test.Test

class LoginViewModelTest : ViewModelTest() {

    @Test
    fun `successful login emits NavigateToDashboard event`() {
        val sdk = fakeSdk {
            auth {
                login { _, _ ->
                    Either.Right(AuthResponse(accessToken = "tok", refreshToken = "ref", expiresIn = 3600))
                }
            }
        }
        val viewModel = LoginViewModel(sdk)
        viewModel.test {
            intent(LoginIntent.EmailChanged("user@test.com"))
            model(LoginModel(email = "user@test.com"))
            intent(LoginIntent.PasswordChanged("password123"))
            model(LoginModel(email = "user@test.com", password = "password123"))
            intent(LoginIntent.SubmitLoginClicked)
            model(LoginModel(email = "user@test.com", password = "password123", isLoading = true))
            event(LoginEvent.NavigateToDashboard)
        }
    }

    @Test
    fun `blank email shows validation error`() {
        val sdk = fakeSdk()
        val viewModel = LoginViewModel(sdk)
        viewModel.test {
            intent(LoginIntent.SubmitLoginClicked)
            model(LoginModel(emailError = "Email must not be blank", passwordError = "Password must not be blank"))
        }
    }

    @Test
    fun `server error shows error in model`() {
        val sdk = fakeSdk {
            auth {
                login { _, _ ->
                    Either.Left(AppError.Auth.InvalidCredentials())
                }
            }
        }
        val viewModel = LoginViewModel(sdk)
        viewModel.test {
            intent(LoginIntent.EmailChanged("user@test.com"))
            model(LoginModel(email = "user@test.com"))
            intent(LoginIntent.PasswordChanged("password123"))
            model(LoginModel(email = "user@test.com", password = "password123"))
            intent(LoginIntent.SubmitLoginClicked)
            // Note: isLoading=true intermediate state is conflated by StateFlow because
            // the fake SDK returns synchronously, so SetServerError follows immediately.
            model(LoginModel(email = "user@test.com", password = "password123", serverError = "Email or password is incorrect"))
        }
    }
}
