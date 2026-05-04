package com.m2f.template.app.privacy

import arrow.core.Either
import com.m2f.template.core.testing.ViewModelTest
import com.m2f.template.core.testing.fakes.fakeSdk
import com.m2f.template.core.testing.test
import com.m2f.template.models.AppError
import com.m2f.template.models.UserRole
import com.m2f.template.models.dto.UserResponse
import com.m2f.template.models.dto.privacy.DeletionResponse
import com.m2f.template.models.dto.privacy.DeletionStatus
import com.m2f.template.models.dto.privacy.VerifyPasswordResponse
import com.m2f.template.models.localization.StringKey
import kotlin.test.Test

private const val TOKEN = "tok-123"

class AccountDeletionViewModelTest : ViewModelTest() {

    @Test
    fun `load checks for pending deletion and shows SCHEDULED step`() {
        val pendingDeletion = DeletionResponse(
            id = "del-123",
            status = DeletionStatus.PENDING,
            scheduledAt = "2026-03-21T00:00:00Z",
        )
        val sdk = fakeSdk {
            privacy {
                getDeletionStatus { Either.Right(pendingDeletion) }
            }
        }
        val viewModel = AccountDeletionViewModel(sdk)
        viewModel.test {
            intent(AccountDeletionIntent.Load)
            model(
                AccountDeletionModel(
                    step = DeletionStep.SCHEDULED,
                    pendingDeletion = pendingDeletion,
                )
            )
        }
    }

    @Test
    fun `load with no pending deletion stays at WARNING`() {
        val sdk = fakeSdk {
            privacy {
                getDeletionStatus { Either.Right(null) }
            }
        }
        val viewModel = AccountDeletionViewModel(sdk)
        viewModel.test {
            intent(AccountDeletionIntent.Load)
            // No model change expected -- state stays at default WARNING
        }
    }

    @Test
    fun `reAuthenticate sets confirmationToken and advances to REASON step`() {
        val sdk = fakeSdk {
            privacy {
                getDeletionStatus { Either.Right(null) }
                verifyPasswordForDeletion { Either.Right(VerifyPasswordResponse(confirmationToken = TOKEN)) }
            }
        }
        val viewModel = AccountDeletionViewModel(sdk)
        viewModel.test {
            intent(AccountDeletionIntent.ReAuthenticate("my-password"))
            model(
                AccountDeletionModel(
                    step = DeletionStep.REASON,
                    confirmationToken = TOKEN,
                )
            )
        }
    }

    @Test
    fun `setReason updates reason and advances to CONFIRM step`() {
        val sdk = fakeSdk {
            privacy {
                getDeletionStatus { Either.Right(null) }
                verifyPasswordForDeletion { Either.Right(VerifyPasswordResponse(confirmationToken = TOKEN)) }
            }
        }
        val viewModel = AccountDeletionViewModel(sdk)
        viewModel.test {
            intent(AccountDeletionIntent.ReAuthenticate("my-password"))
            model(
                AccountDeletionModel(
                    step = DeletionStep.REASON,
                    confirmationToken = TOKEN,
                )
            )
            intent(AccountDeletionIntent.SetReason("No longer needed"))
            model(
                AccountDeletionModel(
                    step = DeletionStep.CONFIRM,
                    confirmationToken = TOKEN,
                    reason = "No longer needed",
                )
            )
        }
    }

    @Test
    fun `confirmDeletion calls SDK logs out and navigates to login`() {
        val deletionResponse = DeletionResponse(
            id = "del-456",
            status = DeletionStatus.PENDING,
            scheduledAt = "2026-03-21T00:00:00Z",
        )
        val sdk = fakeSdk {
            auth { logout { Either.Right(Unit) } }
            privacy {
                getDeletionStatus { Either.Right(null) }
                verifyPasswordForDeletion { Either.Right(VerifyPasswordResponse(confirmationToken = TOKEN)) }
                requestAccountDeletion { Either.Right(deletionResponse) }
            }
        }
        val viewModel = AccountDeletionViewModel(sdk)
        viewModel.test {
            intent(AccountDeletionIntent.ReAuthenticate("my-password"))
            model(
                AccountDeletionModel(
                    step = DeletionStep.REASON,
                    confirmationToken = TOKEN,
                )
            )
            intent(AccountDeletionIntent.SetReason("No longer needed"))
            model(
                AccountDeletionModel(
                    step = DeletionStep.CONFIRM,
                    confirmationToken = TOKEN,
                    reason = "No longer needed",
                )
            )
            intent(AccountDeletionIntent.ConfirmDeletion)
            event(AccountDeletionEvent.NavigateToLogin)
        }
    }

    @Test
    fun `proceedToReAuth advances to RE_AUTH step without setting confirmationToken`() {
        val sdk = fakeSdk {
            privacy {
                getDeletionStatus { Either.Right(null) }
            }
        }
        val viewModel = AccountDeletionViewModel(sdk)
        viewModel.test {
            intent(AccountDeletionIntent.ProceedToReAuth)
            model(
                AccountDeletionModel(
                    step = DeletionStep.RE_AUTH,
                )
            )
        }
    }

    @Test
    fun `full deletion flow with proceedToReAuth`() {
        val deletionResponse = DeletionResponse(
            id = "del-789",
            status = DeletionStatus.PENDING,
            scheduledAt = "2026-03-21T00:00:00Z",
        )
        val sdk = fakeSdk {
            auth { logout { Either.Right(Unit) } }
            privacy {
                getDeletionStatus { Either.Right(null) }
                verifyPasswordForDeletion { Either.Right(VerifyPasswordResponse(confirmationToken = TOKEN)) }
                requestAccountDeletion { Either.Right(deletionResponse) }
            }
        }
        val viewModel = AccountDeletionViewModel(sdk)
        viewModel.test {
            intent(AccountDeletionIntent.ProceedToReAuth)
            model(
                AccountDeletionModel(
                    step = DeletionStep.RE_AUTH,
                )
            )
            intent(AccountDeletionIntent.ReAuthenticate("my-password"))
            model(
                AccountDeletionModel(
                    step = DeletionStep.REASON,
                    confirmationToken = TOKEN,
                )
            )
            intent(AccountDeletionIntent.SetReason("reason"))
            model(
                AccountDeletionModel(
                    step = DeletionStep.CONFIRM,
                    confirmationToken = TOKEN,
                    reason = "reason",
                )
            )
            intent(AccountDeletionIntent.ConfirmDeletion)
            event(AccountDeletionEvent.NavigateToLogin)
        }
    }

    @Test
    fun `skip reason moves to confirm step with empty reason`() {
        val sdk = fakeSdk {
            privacy {
                getDeletionStatus { Either.Right(null) }
            }
        }
        val viewModel = AccountDeletionViewModel(sdk)
        viewModel.test {
            intent(AccountDeletionIntent.SkipReason)
            model(
                AccountDeletionModel(
                    step = DeletionStep.CONFIRM,
                    reason = "",
                )
            )
        }
    }

    @Test
    fun `log out triggers logout and sends LoggedOut event`() {
        val sdk = fakeSdk {
            auth { logout { Either.Right(Unit) } }
        }
        val viewModel = AccountDeletionViewModel(sdk)
        viewModel.test {
            // Set up state through prior steps to ensure pipeline is warm
            intent(AccountDeletionIntent.ProceedToReAuth)
            model(AccountDeletionModel(step = DeletionStep.RE_AUTH))
            intent(AccountDeletionIntent.LogOut)
            event(AccountDeletionEvent.LoggedOut)
        }
    }

    @Test
    fun `load fetches user email from profile`() {
        val profile = UserResponse(
            id = "user-1",
            email = "test@example.com",
            name = "Test User",
            role = UserRole.User,
        )
        val sdk = fakeSdk {
            user { getProfile { Either.Right(profile) } }
            privacy {
                getDeletionStatus { Either.Right(null) }
            }
        }
        val viewModel = AccountDeletionViewModel(sdk)
        viewModel.test {
            intent(AccountDeletionIntent.Load)
            model(
                AccountDeletionModel(
                    userEmail = "test@example.com",
                )
            )
        }
    }

    @Test
    fun `cancelDeletion calls SDK and emits DeletionCancelled`() {
        val pendingDeletion = DeletionResponse(
            id = "del-123",
            status = DeletionStatus.PENDING,
            scheduledAt = "2026-03-21T00:00:00Z",
        )
        val sdk = fakeSdk {
            privacy {
                getDeletionStatus { Either.Right(pendingDeletion) }
                cancelDeletion { Either.Right(Unit) }
            }
        }
        val viewModel = AccountDeletionViewModel(sdk)
        viewModel.test {
            intent(AccountDeletionIntent.Load)
            model(
                AccountDeletionModel(
                    step = DeletionStep.SCHEDULED,
                    pendingDeletion = pendingDeletion,
                    loading = false,
                )
            )
            intent(AccountDeletionIntent.CancelDeletion)
            model(AccountDeletionModel())
            event(AccountDeletionEvent.DeletionCancelled)
        }
    }
}
