package com.m2f.template.app.auth.wire

import com.m2f.template.app.auth.ForgotPasswordViewModel
import com.m2f.template.app.auth.InviteAcceptViewModel
import com.m2f.template.app.auth.LoginViewModel
import com.m2f.template.app.auth.RegisterViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::ForgotPasswordViewModel)
    viewModelOf(::InviteAcceptViewModel)
}
