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
import com.m2f.template.storage.TokenStorage
import com.m2f.template.sdk.AuthInterceptor
import androidx.navigation.compose.rememberNavController
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

@Composable
fun App() {
    KoinApplication(application = {
        modules(allAppModules)
    }) {
        val preferencesStorage = koinInject<PreferencesStorage>()
        val tokenStorage = koinInject<TokenStorage>()
        val authInterceptor = koinInject<AuthInterceptor>()
        val currentLocale by preferencesStorage.observeLanguage()
            .collectAsState(initial = preferencesStorage.language)

        LaunchedEffect(currentLocale) {
            setAppLocale(currentLocale)
        }

        // NavController lives OUTSIDE key(currentLocale) so it survives locale changes
        val navController = rememberNavController()

        CompositionLocalProvider(LocalAppLocale provides currentLocale) {
            key(currentLocale) {
                TerminalTheme {
                    AppNavHost(
                        navController = navController,
                        tokenStorage = tokenStorage,
                        authInterceptor = authInterceptor,
                    )
                }
            }
        }
    }
}
