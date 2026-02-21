package com.m2f.template.localization

import kotlin.js.ExperimentalWasmJsInterop

// WASM locale switching works via a JS shim that monkey-patches Navigator.prototype.languages
// to read from window.__customLocale. When the user switches locale, we set __customLocale
// and Compose Resources picks it up on next recomposition (via key(currentLocale) in App.kt).
// See: https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-resource-environment.html

/**
 * External reference to the window object for setting __customLocale.
 */
@OptIn(ExperimentalWasmJsInterop::class)
private fun setCustomLocale(locale: JsString): Unit = js("window.__customLocale = locale")

@OptIn(ExperimentalWasmJsInterop::class)
private fun clearCustomLocale(): Unit = js("window.__customLocale = null")

@OptIn(ExperimentalWasmJsInterop::class)
private fun getCustomLocale(): JsString? = js("window.__customLocale")

@OptIn(ExperimentalWasmJsInterop::class)
private fun getLocalStorageLocale(): JsString? =
    js("localStorage.getItem('com.russhwolf.settings.pref_language')")

private var overrideLocale: String? = null

actual fun setAppLocale(languageTag: String) {
    overrideLocale = languageTag
    // Set window.__customLocale so the index.html shim makes navigator.languages
    // return this locale. Compose Resources reads navigator.languages via Locale.current.
    @OptIn(ExperimentalWasmJsInterop::class)
    setCustomLocale(languageTag.toJsString())
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun navigatorLanguage(): JsString = js("navigator.language")

@OptIn(ExperimentalWasmJsInterop::class)
private fun browserLanguage(): String = navigatorLanguage().toString().take(2)

@OptIn(ExperimentalWasmJsInterop::class)
actual fun getAppLocale(): String =
    overrideLocale
        ?: getLocalStorageLocale()?.toString()
        ?: browserLanguage()
