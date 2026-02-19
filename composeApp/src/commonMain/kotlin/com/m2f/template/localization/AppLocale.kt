package com.m2f.template.localization

/**
 * Sets the application locale for the current platform.
 *
 * On Android/JVM: sets [java.util.Locale.setDefault].
 * On iOS: writes to NSUserDefaults "AppleLanguages".
 * On WASM: stores the preference in memory (page reload needed for full effect).
 *
 * @param languageTag BCP-47 language tag (e.g. "en", "es").
 */
expect fun setAppLocale(languageTag: String)

/**
 * Returns the current application locale as a BCP-47 language tag.
 */
expect fun getAppLocale(): String
