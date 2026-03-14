package com.m2f.template.app.privacy.wire

import com.m2f.template.app.privacy.AccountDeletionViewModel
import com.m2f.template.app.privacy.ConsentGateViewModel
import com.m2f.template.app.privacy.LegalDocumentViewModel
import com.m2f.template.app.privacy.PrivacySettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val privacyModule = module {
    viewModelOf(::ConsentGateViewModel)
    viewModelOf(::PrivacySettingsViewModel)
    viewModelOf(::LegalDocumentViewModel)
    viewModelOf(::AccountDeletionViewModel)
}
