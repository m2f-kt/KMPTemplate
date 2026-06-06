package com.m2f.template

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import com.m2f.template.designsystem.theme.AuraTheme
import com.m2f.template.theme.rememberSystemDarkTheme
import com.m2f.template.di.allAppModules
import com.m2f.template.localization.LocalAppLocale
import com.m2f.template.localization.setAppLocale
import com.m2f.template.navigation.AppNavHost
import com.m2f.template.app.auth.contract.InviteAcceptRoute
import com.m2f.template.app.auth.contract.LoginRoute
import com.m2f.template.app.auth.contract.OAuthCallbackRoute
import com.m2f.template.app.dashboard.contract.DashboardRoute
import com.m2f.template.navigation.Route
import com.m2f.template.app.auth.wire.checkOAuthCallback
import com.m2f.template.app.auth.wire.checkInviteLink
import com.m2f.template.storage.PreferencesStorage
import com.m2f.template.storage.TokenStorage
import com.m2f.template.sdk.AuthInterceptor
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

        // Back stack lives OUTSIDE key(currentLocale) so it survives locale changes
        val backStack = remember { mutableStateListOf<Route>(LoginRoute()) }

        // --- Auth effects: MUST be outside key(currentLocale) ---
        // These run once on app startup. If they were inside key(), locale changes
        // would destroy/recreate the composable tree, re-triggering clearSessionTokens()
        // and wiping non-rememberMe session tokens.

        // Check for deep links FIRST - they take priority over auto-login
        // Check for invite link on startup (WASM: browser URL /invite/accept?token=...)
        LaunchedEffect(Unit) {
            val token = checkInviteLink()
            if (token != null) {
                backStack.clear()
                backStack.add(InviteAcceptRoute(token = token))
                return@LaunchedEffect
            }

            // Check for OAuth callback on startup (WASM: browser URL params)
            val callback = checkOAuthCallback()
            if (callback != null) {
                backStack.clear()
                backStack.add(
                    OAuthCallbackRoute(
                        accessToken = callback.first,
                        refreshToken = callback.second,
                    ),
                )
                return@LaunchedEffect
            }

            // Clear session-only tokens on startup; if rememberMe tokens survive, skip login
            tokenStorage.clearSessionTokens()
            val accessToken = tokenStorage.getAccessToken()
            if (accessToken != null) {
                backStack.clear()
                backStack.add(DashboardRoute)
            }
        }

        // Navigate to login when session expires (refresh token failed)
        LaunchedEffect(Unit) {
            authInterceptor.sessionExpired.collect {
                backStack.clear()
                backStack.add(LoginRoute())
            }
        }

        CompositionLocalProvider(LocalAppLocale provides currentLocale) {
            key(currentLocale) {
                AuraTheme(darkTheme = rememberSystemDarkTheme()) {
                    AppNavHost(
                        backStack = backStack,
                    )
                }
            }
        }
    }
}
