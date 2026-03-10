package com.m2f.template.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.m2f.template.app.admin.wire.adminEntries
import com.m2f.template.app.auth.wire.authEntries
import com.m2f.template.app.dashboard.wire.dashboardEntries
import com.m2f.template.app.documents.wire.documentsEntries
import com.m2f.template.app.profile.wire.profileEntries
import com.m2f.template.localization.LocalAppLocale
import com.m2f.template.localization.LocaleSelector
import com.m2f.template.storage.PreferencesStorage
import org.koin.compose.koinInject

@Composable
fun AppNavHost(
    backStack: MutableList<Route>,
) {
    val preferencesStorage = koinInject<PreferencesStorage>()
    val currentLocale = LocalAppLocale.current

    Box(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryProvider = entryProvider {
                authEntries(backStack)
                dashboardEntries(backStack)
                profileEntries(backStack) {
                    LocaleSelector(
                        currentLocale = currentLocale,
                        onLocaleChanged = { locale ->
                            preferencesStorage.language = locale
                        },
                    )
                }
                adminEntries(backStack)
                documentsEntries(backStack)
            },
        )
    }
}
