package com.m2f.template.di

import com.m2f.template.app.admin.AdminPanelViewModel
import com.m2f.template.app.auth.ForgotPasswordViewModel
import com.m2f.template.app.auth.LoginViewModel
import com.m2f.template.app.auth.RegisterViewModel
import com.m2f.template.app.dashboard.DashboardViewModel
import com.m2f.template.app.profile.ProfileViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Client application DI module.
 * Combines shared module with client-specific dependencies.
 */
val appModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::ForgotPasswordViewModel)
    viewModelOf(::DashboardViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::AdminPanelViewModel)
}

/**
 * All modules that make up the client application DI graph.
 */
val allAppModules = listOf(
    sharedModule,
    appModule,
)
