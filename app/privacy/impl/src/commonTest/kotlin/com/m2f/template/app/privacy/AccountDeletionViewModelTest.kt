package com.m2f.template.app.privacy

import arrow.core.Either
import com.m2f.template.core.testing.ViewModelTest
import com.m2f.template.core.testing.fakes.fakeSdk
import com.m2f.template.core.testing.test
import com.m2f.template.models.AppError
import com.m2f.template.models.dto.privacy.DeletionResponse
import com.m2f.template.models.dto.privacy.DeletionStatus
import com.m2f.template.models.localization.StringKey
import kotlin.test.Test

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
    fun `reAuthenticate sets password and advances to REASON step`() {
        val sdk = fakeSdk {
            privacy {
                getDeletionStatus { Either.Right(null) }
            }
        }
        val viewModel = AccountDeletionViewModel(sdk)
        viewModel.test {
            intent(AccountDeletionIntent.ReAuthenticate("my-password"))
            model(
                AccountDeletionModel(
                    step = DeletionStep.REASON,
                    password = "my-password",
                )
            )
        }
    }

    @Test
    fun `setReason updates reason and advances to CONFIRM step`() {
        val sdk = fakeSdk {
            privacy {
                getDeletionStatus { Either.Right(null) }
            }
        }
        val viewModel = AccountDeletionViewModel(sdk)
        viewModel.test {
            intent(AccountDeletionIntent.ReAuthenticate("my-password"))
            model(
                AccountDeletionModel(
                    step = DeletionStep.REASON,
                    password = "my-password",
                )
            )
            intent(AccountDeletionIntent.SetReason("No longer needed"))
            model(
                AccountDeletionModel(
                    step = DeletionStep.CONFIRM,
                    password = "my-password",
                    reason = "No longer needed",
                )
            )
        }
    }

    @Test
    fun `confirmDeletion calls SDK and emits DeletionScheduled`() {
        val deletionResponse = DeletionResponse(
            id = "del-456",
            status = DeletionStatus.PENDING,
            scheduledAt = "2026-03-21T00:00:00Z",
        )
        val sdk = fakeSdk {
            privacy {
                getDeletionStatus { Either.Right(null) }
                requestAccountDeletion { Either.Right(deletionResponse) }
            }
        }
        val viewModel = AccountDeletionViewModel(sdk)
        viewModel.test {
            intent(AccountDeletionIntent.ReAuthenticate("my-password"))
            model(
                AccountDeletionModel(
                    step = DeletionStep.REASON,
                    password = "my-password",
                )
            )
            intent(AccountDeletionIntent.SetReason("No longer needed"))
            model(
                AccountDeletionModel(
                    step = DeletionStep.CONFIRM,
                    password = "my-password",
                    reason = "No longer needed",
                )
            )
            intent(AccountDeletionIntent.ConfirmDeletion)
            event(AccountDeletionEvent.DeletionScheduled)
        }
    }

    @Test
    fun `proceedToReAuth advances to RE_AUTH step without setting password`() {
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
                    password = "",
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
            privacy {
                getDeletionStatus { Either.Right(null) }
                requestAccountDeletion { Either.Right(deletionResponse) }
            }
        }
        val viewModel = AccountDeletionViewModel(sdk)
        viewModel.test {
            intent(AccountDeletionIntent.ProceedToReAuth)
            model(
                AccountDeletionModel(
                    step = DeletionStep.RE_AUTH,
                    password = "",
                )
            )
            intent(AccountDeletionIntent.ReAuthenticate("my-password"))
            model(
                AccountDeletionModel(
                    step = DeletionStep.REASON,
                    password = "my-password",
                )
            )
            intent(AccountDeletionIntent.SetReason("reason"))
            model(
                AccountDeletionModel(
                    step = DeletionStep.CONFIRM,
                    password = "my-password",
                    reason = "reason",
                )
            )
            intent(AccountDeletionIntent.ConfirmDeletion)
            event(AccountDeletionEvent.DeletionScheduled)
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
            event(AccountDeletionEvent.DeletionCancelled)
        }
    }
}
