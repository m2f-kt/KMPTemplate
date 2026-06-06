package com.m2f.template.navigation

import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Koin module for `core:navigation`. Registers the app-wide [NavSelectionSignal] used by external
 * entry points to select an in-shell pane on an already-mounted host shell. Included into the shared
 * DI graph via `sharedModule` so `koinInject<NavSelectionSignal>` resolves on every target.
 */
val navigationModule: Module = module {
    single { NavSelectionSignal() }
}
