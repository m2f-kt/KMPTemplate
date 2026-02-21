package com.m2f.template.localization

import kotlin.js.ExperimentalWasmJsInterop

// WASM locale switching is best-effort. The browser's navigator.languages
// determines the Compose Resources locale. We store the preference but
// actual locale switching requires a page reload on web.

private var overrideLocale: String? = null

actual fun setAppLocale(languageTag: String) {
    overrideLocale = languageTag
    // Note: Compose Resources on WASM reads navigator.languages at startup.
    // Runtime override requires page reload — this is a known limitation.
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun navigatorLanguage(): JsString = js("navigator.language")

@OptIn(ExperimentalWasmJsInterop::class)
private fun browserLanguage(): String = navigatorLanguage().toString().take(2)

actual fun getAppLocale(): String =
    overrideLocale ?: browserLanguage()
