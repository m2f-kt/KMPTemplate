package com.m2f.template.app.auth

import arrow.core.Either
import com.m2f.template.core.testing.ViewModelTest
import com.m2f.template.core.testing.fakes.fakeSdk
import com.m2f.template.core.testing.test
import com.m2f.template.models.AppError
import com.m2f.template.models.dto.AuthResponse
import com.m2f.template.models.dto.privacy.ConsentType
import com.m2f.template.models.dto.privacy.RequiredConsent
import com.m2f.template.models.dto.privacy.RequiredConsentsResponse
import com.m2f.template.models.localization.StringKey
import kotlin.test.Test

class RegisterViewModelTest : ViewModelTest() {

    @Test
    fun `successful registration emits NavigateToDashboard event`() {
        val sdk = fakeSdk {
            auth {
                register { Either.Right(AuthResponse(accessToken = "tok", refreshToken = "ref", expiresIn = 3600)) }
            }
            privacy {
                getRequiredConsents {
                    Either.Right(RequiredConsentsResponse(consents = emptyList(), hasOutdated = false))
                }
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
                        "firstName" to StringKey.VALIDATION_NAME_BLANK,
                        "lastName" to StringKey.VALIDATION_NAME_BLANK,
                        "email" to StringKey.VALIDATION_EMAIL_BLANK,
                        "password" to StringKey.VALIDATION_PASSWORD_TOO_SHORT,
                        "terms" to StringKey.VALIDATION_TERMS_NOT_ACCEPTED,
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
            model(RegisterModel(firstName = "John", lastName = "Doe", email = "john@test.com", password = "password123", confirmPassword = "password123", termsAccepted = true, serverError = StringKey.AUTH_USER_ALREADY_EXISTS))
        }
    }

    @Test
    fun `successful registration with outdated consents grants them and navigates to dashboard`() {
        var getRequiredConsentsCallCount = 0
        val sdk = fakeSdk {
            auth {
                register { Either.Right(AuthResponse(accessToken = "tok", refreshToken = "ref", expiresIn = 3600)) }
            }
            privacy {
                getRequiredConsents {
                    getRequiredConsentsCallCount++
                    if (getRequiredConsentsCallCount == 1) {
                        // First call (from grantRequiredConsents): has outdated consents
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
                    } else {
                        // Second call (from navigateWithConsentCheck): consents now granted
                        Either.Right(RequiredConsentsResponse(consents = emptyList(), hasOutdated = false))
                    }
                }
                grantConsent { Either.Right(Unit) }
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
    fun `successful registration with consent check failure navigates to dashboard`() {
        val sdk = fakeSdk {
            auth {
                register { Either.Right(AuthResponse(accessToken = "tok", refreshToken = "ref", expiresIn = 3600)) }
            }
            // privacy not configured -> defaults to Either.Left, so consent check fails
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
}
