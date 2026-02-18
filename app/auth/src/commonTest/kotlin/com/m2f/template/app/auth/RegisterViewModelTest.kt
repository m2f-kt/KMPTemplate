package com.m2f.template.app.auth

import arrow.core.Either
import com.m2f.template.core.testing.ViewModelTest
import com.m2f.template.core.testing.fakes.fakeSdk
import com.m2f.template.core.testing.test
import com.m2f.template.models.AppError
import com.m2f.template.models.dto.AuthResponse
import kotlin.test.Test

class RegisterViewModelTest : ViewModelTest() {

    @Test
    fun `successful registration emits NavigateToDashboard event`() {
        val sdk = fakeSdk {
            auth {
                register { Either.Right(AuthResponse(accessToken = "tok", refreshToken = "ref", expiresIn = 3600)) }
            }
        }
        val viewModel = RegisterViewModel(sdk)
        viewModel.test {
            intent(RegisterIntent.FirstNameChanged("John"))
            model(RegisterModel(firstName = "John"))
            intent(RegisterIntent.LastNameChanged("Doe"))
            model(RegisterModel(firstName = "John", lastName = "Doe"))
            intent(RegisterIntent.EmailChanged("john@test.com"))
            model(RegisterModel(firstName = "John", lastName = "Doe", email = "john@test.com"))
            intent(RegisterIntent.PasswordChanged("password123"))
            model(RegisterModel(firstName = "John", lastName = "Doe", email = "john@test.com", password = "password123"))
            intent(RegisterIntent.ConfirmPasswordChanged("password123"))
            model(RegisterModel(firstName = "John", lastName = "Doe", email = "john@test.com", password = "password123", confirmPassword = "password123"))
            intent(RegisterIntent.TermsAcceptedChanged(true))
            model(RegisterModel(firstName = "John", lastName = "Doe", email = "john@test.com", password = "password123", confirmPassword = "password123", termsAccepted = true))
            intent(RegisterIntent.SubmitRegisterClicked)
            model(RegisterModel(firstName = "John", lastName = "Doe", email = "john@test.com", password = "password123", confirmPassword = "password123", termsAccepted = true, isLoading = true))
            event(RegisterEvent.NavigateToDashboard)
        }
    }

    @Test
    fun `validation errors show in model for blank fields`() {
        val sdk = fakeSdk()
        val viewModel = RegisterViewModel(sdk)
        viewModel.test {
            intent(RegisterIntent.SubmitRegisterClicked)
            model(
                RegisterModel(
                    fieldErrors = mapOf(
                        "firstName" to "Name must not be blank",
                        "lastName" to "Name must not be blank",
                        "email" to "Email must not be blank",
                        "password" to "Password must be at least 8 characters",
                        "terms" to "You must accept the terms",
                    ),
                ),
            )
        }
    }

    @Test
    fun `server error shows error in model`() {
        val sdk = fakeSdk {
            auth {
                register { Either.Left(AppError.Auth.UserAlreadyExists()) }
            }
        }
        val viewModel = RegisterViewModel(sdk)
        viewModel.test {
            intent(RegisterIntent.FirstNameChanged("John"))
            model(RegisterModel(firstName = "John"))
            intent(RegisterIntent.LastNameChanged("Doe"))
            model(RegisterModel(firstName = "John", lastName = "Doe"))
            intent(RegisterIntent.EmailChanged("john@test.com"))
            model(RegisterModel(firstName = "John", lastName = "Doe", email = "john@test.com"))
            intent(RegisterIntent.PasswordChanged("password123"))
            model(RegisterModel(firstName = "John", lastName = "Doe", email = "john@test.com", password = "password123"))
            intent(RegisterIntent.ConfirmPasswordChanged("password123"))
            model(RegisterModel(firstName = "John", lastName = "Doe", email = "john@test.com", password = "password123", confirmPassword = "password123"))
            intent(RegisterIntent.TermsAcceptedChanged(true))
            model(RegisterModel(firstName = "John", lastName = "Doe", email = "john@test.com", password = "password123", confirmPassword = "password123", termsAccepted = true))
            intent(RegisterIntent.SubmitRegisterClicked)
            // Note: isLoading=true intermediate state is conflated by StateFlow because
            // the fake SDK returns synchronously, so SetServerError follows immediately.
            model(RegisterModel(firstName = "John", lastName = "Doe", email = "john@test.com", password = "password123", confirmPassword = "password123", termsAccepted = true, serverError = "A user with this email already exists"))
        }
    }
}
