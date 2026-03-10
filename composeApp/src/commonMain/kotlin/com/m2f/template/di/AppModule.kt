package com.m2f.template.di

import com.m2f.template.app.admin.AdminPanelViewModel
import com.m2f.template.app.admin.RegisterMemberViewModel
import com.m2f.template.app.auth.ForgotPasswordViewModel
import com.m2f.template.app.auth.InviteAcceptViewModel
import com.m2f.template.app.auth.LoginViewModel
import com.m2f.template.app.auth.RegisterViewModel
import com.m2f.template.app.dashboard.wire.dashboardModule
import com.m2f.template.app.documents.wire.documentsModule
import com.m2f.template.app.profile.wire.profileModule
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Client application DI module.
 * Combines shared module with client-specific dependencies.
 */
val appModule = module {
    includes(documentsModule)
    includes(dashboardModule)
    includes(profileModule)
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::ForgotPasswordViewModel)
    viewModelOf(::AdminPanelViewModel)
    viewModelOf(::RegisterMemberViewModel)
    viewModelOf(::InviteAcceptViewModel)
}

/**
 * All modules that make up the client application DI graph.
 */
val allAppModules = listOf(
    sharedModule,
    appModule,
)
