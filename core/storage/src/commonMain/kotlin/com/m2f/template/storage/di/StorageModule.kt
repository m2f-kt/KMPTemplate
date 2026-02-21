package com.m2f.template.storage.di

import com.m2f.template.storage.PreferencesStorage
import com.m2f.template.storage.TokenStorage
import com.russhwolf.settings.Settings
import org.koin.dsl.module

val storageModule = module {
    single<Settings> { Settings() }
    single { TokenStorage(settings = get<Settings>()) }
    single { PreferencesStorage(settings = get<Settings>()) }
}
