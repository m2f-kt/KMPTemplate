package com.m2f.template.app.privacy

import arrow.core.Either
import com.m2f.template.core.testing.ViewModelTest
import com.m2f.template.core.testing.fakes.fakeSdk
import com.m2f.template.core.testing.test
import com.m2f.template.models.AppError
import com.m2f.template.models.dto.privacy.ConsentType
import com.m2f.template.models.dto.privacy.RequiredConsent
import com.m2f.template.models.dto.privacy.RequiredConsentsResponse
import com.m2f.template.models.localization.StringKey
import kotlin.test.Test

class ConsentGateViewModelTest : ViewModelTest() {

    @Test
    fun `loading required consents shows consent items`() {
        val sdk = fakeSdk {
            privacy {
                getRequiredConsents {
                    Either.Right(
                        RequiredConsentsResponse(
                            consents = listOf(
                                RequiredConsent(
                                    type = ConsentType.PRIVACY_POLICY,
                                    currentVersion = "1.0",
                                    acceptedVersion = null,
                                    needsUpdate = true,
                                ),
                                RequiredConsent(
                                    type = ConsentType.TERMS_OF_SERVICE,
                                    currentVersion = "1.0",
                                    acceptedVersion = "1.0",
                                    needsUpdate = false,
                                ),
                            ),
                            hasOutdated = true,
                        )
                    )
                }
            }
        }
        val viewModel = ConsentGateViewModel(sdk)
        viewModel.test {
            intent(ConsentGateIntent.LoadRequiredConsents)
            model(
                ConsentGateModel(
                    consents = listOf(
                        ConsentItem(type = ConsentType.PRIVACY_POLICY, currentVersion = "1.0", accepted = false),
                        ConsentItem(type = ConsentType.TERMS_OF_SERVICE, currentVersion = "1.0", accepted = true),
                    ),
                    allAccepted = false,
                    loading = false,
                    error = null,
                )
            )
        }
    }

    @Test
    fun `toggling consent updates accepted state and allAccepted`() {
        val sdk = fakeSdk {
            privacy {
                getRequiredConsents {
                    Either.Right(
                        RequiredConsentsResponse(
                            consents = listOf(
                                RequiredConsent(
                                    type = ConsentType.PRIVACY_POLICY,
                                    currentVersion = "1.0",
                                    acceptedVersion = null,
                                    needsUpdate = true,
                                ),
                                RequiredConsent(
                                    type = ConsentType.TERMS_OF_SERVICE,
                                    currentVersion = "1.0",
                                    acceptedVersion = null,
                                    needsUpdate = true,
                                ),
                            ),
                            hasOutdated = true,
                        )
                    )
                }
            }
        }
        val viewModel = ConsentGateViewModel(sdk)
        viewModel.test {
            intent(ConsentGateIntent.LoadRequiredConsents)
            model(
                ConsentGateModel(
                    consents = listOf(
                        ConsentItem(type = ConsentType.PRIVACY_POLICY, currentVersion = "1.0", accepted = false),
                        ConsentItem(type = ConsentType.TERMS_OF_SERVICE, currentVersion = "1.0", accepted = false),
                    ),
                    allAccepted = false,
                    loading = false,
                    error = null,
                )
            )
            intent(ConsentGateIntent.ToggleConsent(ConsentType.PRIVACY_POLICY))
            model(
                ConsentGateModel(
                    consents = listOf(
                        ConsentItem(type = ConsentType.PRIVACY_POLICY, currentVersion = "1.0", accepted = true),
                        ConsentItem(type = ConsentType.TERMS_OF_SERVICE, currentVersion = "1.0", accepted = false),
                    ),
                    allAccepted = false,
                    loading = false,
                    error = null,
                )
            )
            intent(ConsentGateIntent.ToggleConsent(ConsentType.TERMS_OF_SERVICE))
            model(
                ConsentGateModel(
                    consents = listOf(
                        ConsentItem(type = ConsentType.PRIVACY_POLICY, currentVersion = "1.0", accepted = true),
                        ConsentItem(type = ConsentType.TERMS_OF_SERVICE, currentVersion = "1.0", accepted = true),
                    ),
                    allAccepted = true,
                    loading = false,
                    error = null,
                )
            )
        }
    }

    @Test
    fun `accept all grants all consents and emits ConsentCompleted`() {
        val sdk = fakeSdk {
            privacy {
                getRequiredConsents {
                    Either.Right(
                        RequiredConsentsResponse(
                            consents = listOf(
                                RequiredConsent(
                                    type = ConsentType.PRIVACY_POLICY,
                                    currentVersion = "1.0",
                                    acceptedVersion = null,
                                    needsUpdate = true,
                                ),
                                RequiredConsent(
                                    type = ConsentType.TERMS_OF_SERVICE,
                                    currentVersion = "1.0",
                                    acceptedVersion = null,
                                    needsUpdate = true,
                                ),
                            ),
                            hasOutdated = true,
                        )
                    )
                }
                grantConsent { Either.Right(Unit) }
            }
        }
        val viewModel = ConsentGateViewModel(sdk)
        viewModel.test {
            intent(ConsentGateIntent.LoadRequiredConsents)
            model(
                ConsentGateModel(
                    consents = listOf(
                        ConsentItem(type = ConsentType.PRIVACY_POLICY, currentVersion = "1.0", accepted = false),
                        ConsentItem(type = ConsentType.TERMS_OF_SERVICE, currentVersion = "1.0", accepted = false),
                    ),
                    allAccepted = false,
                    loading = false,
                    error = null,
                )
            )
            intent(ConsentGateIntent.AcceptAll)
            event(ConsentGateEvent.ConsentCompleted)
        }
    }

    @Test
    fun `view document emits NavigateToDocument event`() {
        val sdk = fakeSdk {
            privacy {
                getRequiredConsents {
                    Either.Right(
                        RequiredConsentsResponse(
                            consents = listOf(
                                RequiredConsent(
                                    type = ConsentType.PRIVACY_POLICY,
                                    currentVersion = "1.0",
                                    acceptedVersion = null,
                                    needsUpdate = true,
                                ),
                            ),
                            hasOutdated = true,
                        )
                    )
                }
            }
        }
        val viewModel = ConsentGateViewModel(sdk)
        viewModel.test {
            intent(ConsentGateIntent.LoadRequiredConsents)
            model(
                ConsentGateModel(
                    consents = listOf(
                        ConsentItem(type = ConsentType.PRIVACY_POLICY, currentVersion = "1.0", accepted = false),
                    ),
                    allAccepted = false,
                    loading = false,
                    error = null,
                )
            )
            intent(ConsentGateIntent.ViewDocument(ConsentType.PRIVACY_POLICY))
            event(ConsentGateEvent.NavigateToDocument(ConsentType.PRIVACY_POLICY))
        }
    }

    @Test
    fun `loading consents with error shows error in model`() {
        val sdk = fakeSdk {
            privacy {
                getRequiredConsents {
                    Either.Left(AppError.Client.Unknown())
                }
            }
        }
        val viewModel = ConsentGateViewModel(sdk)
        viewModel.test {
            intent(ConsentGateIntent.LoadRequiredConsents)
            model(
                ConsentGateModel(
                    consents = emptyList(),
                    allAccepted = false,
                    loading = false,
                    error = StringKey.CLIENT_UNKNOWN_ERROR,
                )
            )
        }
    }
}
