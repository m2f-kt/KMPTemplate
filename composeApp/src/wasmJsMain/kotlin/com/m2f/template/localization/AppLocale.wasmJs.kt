package com.m2f.template.localization

// WASM locale switching is best-effort. The browser's navigator.languages
// determines the Compose Resources locale. We store the preference but
// actual locale switching requires a page reload on web.

private var overrideLocale: String? = null

actual fun setAppLocale(languageTag: String) {
    overrideLocale = languageTag
    // Note: Compose Resources on WASM reads navigator.languages at startup.
    // Runtime override requires page reload — this is a known limitation.
}

actual fun getAppLocale(): String =
    overrideLocale ?: js("navigator.language").toString().take(2)
