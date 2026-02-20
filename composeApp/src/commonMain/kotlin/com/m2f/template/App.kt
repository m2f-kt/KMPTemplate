package com.m2f.template

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import com.m2f.template.designsystem.theme.TerminalTheme
import com.m2f.template.di.allAppModules
import com.m2f.template.localization.LocalAppLocale
import com.m2f.template.localization.setAppLocale
import com.m2f.template.navigation.AppNavHost
import com.m2f.template.storage.PreferencesStorage
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

@Composable
fun App() {
    KoinApplication(application = {
        modules(allAppModules)
    }) {
        val preferencesStorage = koinInject<PreferencesStorage>()
        val currentLocale by preferencesStorage.observeLanguage()
            .collectAsState(initial = preferencesStorage.language)

        LaunchedEffect(currentLocale) {
            setAppLocale(currentLocale)
        }

        CompositionLocalProvider(LocalAppLocale provides currentLocale) {
            key(currentLocale) {
                TerminalTheme {
                    AppNavHost()
                }
            }
        }
    }
}
