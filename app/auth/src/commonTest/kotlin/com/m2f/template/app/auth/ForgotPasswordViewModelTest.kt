package com.m2f.template.app.auth

import arrow.core.Either
import com.m2f.template.core.testing.ViewModelTest
import com.m2f.template.core.testing.fakes.fakeSdk
import com.m2f.template.core.testing.test
import com.m2f.template.models.AppError
import kotlin.test.Test

class ForgotPasswordViewModelTest : ViewModelTest() {

    @Test
    fun `successful forgot password sets emailSent in model`() {
        val sdk = fakeSdk {
            auth {
                forgotPassword { Either.Right(Unit) }
            }
        }
        val viewModel = ForgotPasswordViewModel(sdk)
        viewModel.test {
            intent(ForgotPasswordIntent.EmailChanged("user@test.com"))
            model(ForgotPasswordModel(email = "user@test.com"))
            intent(ForgotPasswordIntent.SubmitForgotPasswordClicked)
            // Note: isLoading=true intermediate state is conflated by StateFlow because
            // the fake SDK returns synchronously, so SetEmailSent follows immediately.
            model(ForgotPasswordModel(email = "user@test.com", isLoading = false, emailSent = true))
        }
    }

    @Test
    fun `blank email shows validation error`() {
        val sdk = fakeSdk()
        val viewModel = ForgotPasswordViewModel(sdk)
        viewModel.test {
            intent(ForgotPasswordIntent.SubmitForgotPasswordClicked)
            model(ForgotPasswordModel(emailError = "Email must not be blank"))
        }
    }
}
