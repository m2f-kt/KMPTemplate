package com.m2f.server.auth.di

import com.m2f.server.auth.service.EmailService
import com.m2f.server.auth.service.SmtpEmailService
import org.koin.dsl.module

val emailModule = module {
    single<EmailService> { SmtpEmailService(get()) }
}
