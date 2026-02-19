package com.m2f.template.app.admin

import arrow.core.Either
import com.m2f.template.core.testing.ViewModelTest
import com.m2f.template.core.testing.fakes.fakeSdk
import com.m2f.template.core.testing.test
import com.m2f.template.models.AppError
import com.m2f.template.models.GroupRole
import com.m2f.template.models.dto.MemberResponse
import com.m2f.template.models.localization.StringKey
import kotlin.test.Test

class RegisterMemberViewModelTest : ViewModelTest() {

    private val successMember = MemberResponse(
        userId = "u-new",
        email = "new@test.com",
        name = "New User",
        role = GroupRole.Member,
        joinedAt = "",
    )

    @Test
    fun `field changes update model`() {
        val sdk = fakeSdk()
        val viewModel = RegisterMemberViewModel(sdk)
        viewModel.test {
            intent(RegisterMemberIntent.EmailChanged("a@b.com"))
            model(RegisterMemberModel(email = "a@b.com"))
            intent(RegisterMemberIntent.FirstNameChanged("John"))
            model(RegisterMemberModel(email = "a@b.com", firstName = "John"))
            intent(RegisterMemberIntent.LastNameChanged("Doe"))
            model(RegisterMemberModel(email = "a@b.com", firstName = "John", lastName = "Doe"))
            intent(RegisterMemberIntent.PasswordChanged("password123"))
            model(RegisterMemberModel(email = "a@b.com", firstName = "John", lastName = "Doe", password = "password123"))
            intent(RegisterMemberIntent.RoleChanged(GroupRole.Admin))
            model(RegisterMemberModel(email = "a@b.com", firstName = "John", lastName = "Doe", password = "password123", role = GroupRole.Admin))
        }
    }

    @Test
    fun `submit with empty fields shows validation errors`() {
        val sdk = fakeSdk()
        val viewModel = RegisterMemberViewModel(sdk)
        viewModel.test {
            intent(RegisterMemberIntent.SubmitRegisterMember("g1"))
            model(
                RegisterMemberModel(
                    fieldErrors = mapOf(
                        "firstName" to StringKey.VALIDATION_NAME_BLANK,
                        "lastName" to StringKey.VALIDATION_NAME_BLANK,
                        "email" to StringKey.VALIDATION_EMAIL_BLANK,
                        "password" to StringKey.VALIDATION_PASSWORD_TOO_SHORT,
                    ),
                ),
            )
        }
    }

    @Test
    fun `successful registration emits RegistrationSuccess`() {
        val sdk = fakeSdk {
            group {
                registerMember { _, _ -> Either.Right(successMember) }
            }
        }
        val viewModel = RegisterMemberViewModel(sdk)
        viewModel.test {
            intent(RegisterMemberIntent.FirstNameChanged("New"))
            model(RegisterMemberModel(firstName = "New"))
            intent(RegisterMemberIntent.LastNameChanged("User"))
            model(RegisterMemberModel(firstName = "New", lastName = "User"))
            intent(RegisterMemberIntent.EmailChanged("new@test.com"))
            model(RegisterMemberModel(firstName = "New", lastName = "User", email = "new@test.com"))
            intent(RegisterMemberIntent.PasswordChanged("password123"))
            model(RegisterMemberModel(firstName = "New", lastName = "User", email = "new@test.com", password = "password123"))
            intent(RegisterMemberIntent.SubmitRegisterMember("g1"))
            // Sync fakes conflate SetLoading(true) with RegistrationSuccess event; assert final settled state
            model(RegisterMemberModel(firstName = "New", lastName = "User", email = "new@test.com", password = "password123", isLoading = true))
            event(RegisterMemberEvent.RegistrationSuccess)
        }
    }

    @Test
    fun `server error displays in model`() {
        val sdk = fakeSdk {
            group {
                registerMember { _, _ -> Either.Left(AppError.Group.MemberAlreadyExists(message = "User already in group")) }
            }
        }
        val viewModel = RegisterMemberViewModel(sdk)
        viewModel.test {
            intent(RegisterMemberIntent.FirstNameChanged("New"))
            model(RegisterMemberModel(firstName = "New"))
            intent(RegisterMemberIntent.LastNameChanged("User"))
            model(RegisterMemberModel(firstName = "New", lastName = "User"))
            intent(RegisterMemberIntent.EmailChanged("new@test.com"))
            model(RegisterMemberModel(firstName = "New", lastName = "User", email = "new@test.com"))
            intent(RegisterMemberIntent.PasswordChanged("password123"))
            model(RegisterMemberModel(firstName = "New", lastName = "User", email = "new@test.com", password = "password123"))
            intent(RegisterMemberIntent.SubmitRegisterMember("g1"))
            // Sync fakes: SetLoading(true) followed by SetServerError conflate; assert final settled state
            model(RegisterMemberModel(firstName = "New", lastName = "User", email = "new@test.com", password = "password123", serverError = StringKey.GROUP_MEMBER_ALREADY_EXISTS))
        }
    }

    @Test
    fun `field change clears corresponding field error`() {
        val sdk = fakeSdk()
        val viewModel = RegisterMemberViewModel(sdk)
        viewModel.test {
            // Submit with empty fields to trigger validation errors
            intent(RegisterMemberIntent.SubmitRegisterMember("g1"))
            model(
                RegisterMemberModel(
                    fieldErrors = mapOf(
                        "firstName" to StringKey.VALIDATION_NAME_BLANK,
                        "lastName" to StringKey.VALIDATION_NAME_BLANK,
                        "email" to StringKey.VALIDATION_EMAIL_BLANK,
                        "password" to StringKey.VALIDATION_PASSWORD_TOO_SHORT,
                    ),
                ),
            )
            // Fix firstName — its error should clear
            intent(RegisterMemberIntent.FirstNameChanged("John"))
            model(
                RegisterMemberModel(
                    firstName = "John",
                    fieldErrors = mapOf(
                        "lastName" to StringKey.VALIDATION_NAME_BLANK,
                        "email" to StringKey.VALIDATION_EMAIL_BLANK,
                        "password" to StringKey.VALIDATION_PASSWORD_TOO_SHORT,
                    ),
                ),
            )
        }
    }
}
