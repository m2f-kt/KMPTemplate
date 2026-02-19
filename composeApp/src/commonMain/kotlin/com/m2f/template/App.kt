package com.m2f.template

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.m2f.template.designsystem.theme.TerminalTheme
import com.m2f.template.di.allAppModules
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
        val storedLocale = remember { preferencesStorage.language }

        LaunchedEffect(Unit) {
            setAppLocale(storedLocale)
        }

        TerminalTheme {
            AppNavHost()
        }
    }
}
