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
import com.m2f.template.navigation.DashboardRoute
import com.m2f.template.navigation.LoginRoute
import com.m2f.template.navigation.OAuthCallbackRoute
import com.m2f.template.navigation.InviteAcceptRoute
import com.m2f.template.app.auth.checkOAuthCallback
import com.m2f.template.app.auth.checkInviteLink
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

        // --- Auth effects: MUST be outside key(currentLocale) ---
        // These run once on app startup. If they were inside key(), locale changes
        // would destroy/recreate the composable tree, re-triggering clearSessionTokens()
        // and wiping non-rememberMe session tokens.

        // Clear session-only tokens on startup; if rememberMe tokens survive, skip login
        LaunchedEffect(Unit) {
            tokenStorage.clearSessionTokens()
            val accessToken = tokenStorage.getAccessToken()
            if (accessToken != null) {
                navController.navigate(DashboardRoute) {
                    popUpTo(LoginRoute) { inclusive = true }
                }
            }
        }

        // Check for OAuth callback on startup (WASM: browser URL params)
        LaunchedEffect(Unit) {
            val callback = checkOAuthCallback()
            if (callback != null) {
                navController.navigate(
                    OAuthCallbackRoute(
                        accessToken = callback.first,
                        refreshToken = callback.second,
                    ),
                ) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }

        // Check for invite link on startup (WASM: browser URL /invite/accept?token=...)
        LaunchedEffect(Unit) {
            val token = checkInviteLink()
            if (token != null) {
                navController.navigate(InviteAcceptRoute(token = token)) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }

        // Navigate to login when session expires (refresh token failed)
        LaunchedEffect(Unit) {
            authInterceptor.sessionExpired.collect {
                navController.navigate(LoginRoute) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }

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
