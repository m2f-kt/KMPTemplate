package com.m2f.template.storage.di

import com.m2f.template.storage.PreferencesStorage
import com.m2f.template.storage.TokenStorage
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import org.koin.dsl.module

val storageModule = module {
    single<Settings> { Settings() }
    single<ObservableSettings> { Settings() as ObservableSettings }
    single { TokenStorage(settings = get<Settings>()) }
    single { PreferencesStorage(settings = get<ObservableSettings>()) }
}
