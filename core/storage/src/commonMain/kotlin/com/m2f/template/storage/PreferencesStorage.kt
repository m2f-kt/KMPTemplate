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

    fun observeTheme(): Flow<String> = _theme.asStateFlow()

    fun observeLanguage(): Flow<String> = _language.asStateFlow()

    companion object {
        private const val KEY_THEME = "pref_theme"
        private const val KEY_LANGUAGE = "pref_language"
        private const val DEFAULT_THEME = "system"
        private const val DEFAULT_LANGUAGE = "en"
    }
}
