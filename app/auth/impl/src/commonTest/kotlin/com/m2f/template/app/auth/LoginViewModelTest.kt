package com.m2f.template.app.auth

import arrow.core.Either
import com.m2f.template.core.testing.ViewModelTest
import com.m2f.template.core.testing.fakes.fakeSdk
import com.m2f.template.core.testing.test
import com.m2f.template.models.AppError
import com.m2f.template.models.dto.AuthResponse
import com.m2f.template.models.dto.privacy.RequiredConsent
import com.m2f.template.models.dto.privacy.RequiredConsentsResponse
import com.m2f.template.models.dto.privacy.ConsentType
import com.m2f.template.models.localization.StringKey
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
            privacy {
                getRequiredConsents {
                    Either.Right(RequiredConsentsResponse(consents = emptyList(), hasOutdated = false))
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
            model(LoginModel(emailError = StringKey.VALIDATION_EMAIL_BLANK, passwordError = StringKey.VALIDATION_PASSWORD_BLANK))
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
            model(LoginModel(email = "user@test.com", password = "password123", serverError = StringKey.AUTH_INVALID_CREDENTIALS))
        }
    }

    @Test
    fun `successful login with outdated consents navigates to consent gate`() {
        val sdk = fakeSdk {
            auth {
                login { _, _ ->
                    Either.Right(AuthResponse(accessToken = "tok", refreshToken = "ref", expiresIn = 3600))
                }
            }
            privacy {
                getRequiredConsents {
                    Either.Right(
                        RequiredConsentsResponse(
                            consents = listOf(
                                RequiredConsent(
                                    type = ConsentType.PRIVACY_POLICY,
                                    currentVersion = "2.0",
                                    acceptedVersion = "1.0",
                                    needsUpdate = true,
                                ),
                            ),
                            hasOutdated = true,
                        ),
                    )
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
            event(LoginEvent.NavigateToConsentGate)
        }
    }

    @Test
    fun `reset intent clears all state back to initial`() {
        val sdk = fakeSdk()
        val viewModel = LoginViewModel(sdk)
        viewModel.test {
            intent(LoginIntent.EmailChanged("user@test.com"))
            model(LoginModel(email = "user@test.com"))
            intent(LoginIntent.PasswordChanged("secret"))
            model(LoginModel(email = "user@test.com", password = "secret"))
            intent(LoginIntent.Reset)
            model(LoginModel())
        }
    }

    @Test
    fun `successful login with consent check failure navigates to dashboard`() {
        val sdk = fakeSdk {
            auth {
                login { _, _ ->
                    Either.Right(AuthResponse(accessToken = "tok", refreshToken = "ref", expiresIn = 3600))
                }
            }
            // privacy not configured -> defaults to Either.Left, so consent check fails
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
}
