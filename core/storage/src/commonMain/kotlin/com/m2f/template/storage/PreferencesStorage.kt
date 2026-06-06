package com.m2f.template.storage

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Wrapper around [Settings] that provides observable preference values.
 *
 * Observation is backed by [MutableStateFlow] instead of [ObservableSettings]
 * so that it works uniformly across all KMP targets — including wasmJs where
 * `StorageSettings` does **not** implement `ObservableSettings`.
 */
class PreferencesStorage(private val settings: Settings) {

    private val _theme = MutableStateFlow(settings[KEY_THEME] ?: DEFAULT_THEME)
    private val _language = MutableStateFlow(settings[KEY_LANGUAGE] ?: DEFAULT_LANGUAGE)
    private val _telemetryConsent =
        MutableStateFlow(settings.getBoolean(KEY_TELEMETRY_CONSENT, DEFAULT_TELEMETRY_CONSENT))

    var theme: String
        get() = _theme.value
        set(value) {
            settings[KEY_THEME] = value
            _theme.value = value
        }

    var language: String
        get() = _language.value
        set(value) {
            settings[KEY_LANGUAGE] = value
            _language.value = value
        }

    /**
     * Whether the user consents to anonymous telemetry/diagnostics being collected. Defaults
     * **off** (privacy-first): nothing is reported until the user opts in. A generic, product-
     * agnostic flag — the feature that actually emits telemetry decides what to send when this
     * is `true`. Persisted so the choice survives an app restart.
     */
    var telemetryConsent: Boolean
        get() = _telemetryConsent.value
        set(value) {
            settings.putBoolean(KEY_TELEMETRY_CONSENT, value)
            _telemetryConsent.value = value
        }

    fun observeTheme(): Flow<String> = _theme.asStateFlow()

    fun observeLanguage(): Flow<String> = _language.asStateFlow()

    fun observeTelemetryConsent(): Flow<Boolean> = _telemetryConsent.asStateFlow()

    companion object {
        private const val KEY_THEME = "pref_theme"
        private const val KEY_LANGUAGE = "pref_language"
        private const val KEY_TELEMETRY_CONSENT = "pref_telemetry_consent"
        private const val DEFAULT_THEME = "system"
        private const val DEFAULT_LANGUAGE = "en"

        // Telemetry consent ships OFF (privacy default): nothing is reported until the user opts in.
        private const val DEFAULT_TELEMETRY_CONSENT = false
    }
}
