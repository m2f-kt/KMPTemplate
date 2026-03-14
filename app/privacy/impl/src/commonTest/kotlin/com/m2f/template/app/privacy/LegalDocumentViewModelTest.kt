package com.m2f.template.app.privacy

import arrow.core.Either
import com.m2f.template.core.testing.ViewModelTest
import com.m2f.template.core.testing.fakes.fakeSdk
import com.m2f.template.core.testing.test
import com.m2f.template.models.AppError
import com.m2f.template.models.dto.privacy.ConsentType
import com.m2f.template.models.dto.privacy.LegalDocumentResponse
import com.m2f.template.models.localization.StringKey
import kotlin.test.Test

class LegalDocumentViewModelTest : ViewModelTest() {

    private val sampleDocument = LegalDocumentResponse(
        type = ConsentType.PRIVACY_POLICY,
        version = "1.0",
        locale = "en",
        content = "# Privacy Policy\n\nThis is the privacy policy content.",
        publishedAt = "2026-01-01T00:00:00Z",
    )

    private val spanishDocument = LegalDocumentResponse(
        type = ConsentType.PRIVACY_POLICY,
        version = "1.0",
        locale = "es",
        content = "# Politica de Privacidad\n\nEste es el contenido.",
        publishedAt = "2026-01-01T00:00:00Z",
    )

    @Test
    fun `load fetches legal document and populates model`() {
        val sdk = fakeSdk {
            privacy {
                getLegalDocument { _, _ -> Either.Right(sampleDocument) }
            }
        }
        val viewModel = LegalDocumentViewModel(sdk)
        viewModel.test {
            intent(LegalDocumentIntent.Load(type = "PRIVACY_POLICY", locale = "en"))
            model(
                LegalDocumentModel(
                    document = sampleDocument,
                    loading = false,
                    error = null,
                )
            )
        }
    }

    @Test
    fun `switch locale reloads document with new locale`() {
        val sdk = fakeSdk {
            privacy {
                getLegalDocument { _, locale ->
                    when (locale) {
                        "es" -> Either.Right(spanishDocument)
                        else -> Either.Right(sampleDocument)
                    }
                }
            }
        }
        val viewModel = LegalDocumentViewModel(sdk)
        viewModel.test {
            intent(LegalDocumentIntent.Load(type = "PRIVACY_POLICY", locale = "en"))
            model(
                LegalDocumentModel(
                    document = sampleDocument,
                    loading = false,
                    error = null,
                )
            )
            intent(LegalDocumentIntent.SwitchLocale("es"))
            model(
                LegalDocumentModel(
                    document = spanishDocument,
                    loading = false,
                    error = null,
                )
            )
        }
    }

    @Test
    fun `load with error shows error in model`() {
        val sdk = fakeSdk {
            privacy {
                getLegalDocument { _, _ -> Either.Left(AppError.Client.Unknown()) }
            }
        }
        val viewModel = LegalDocumentViewModel(sdk)
        viewModel.test {
            intent(LegalDocumentIntent.Load(type = "PRIVACY_POLICY"))
            model(
                LegalDocumentModel(
                    document = null,
                    loading = false,
                    error = StringKey.CLIENT_UNKNOWN_ERROR,
                )
            )
        }
    }
}
