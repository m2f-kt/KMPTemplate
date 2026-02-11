package com.m2f.template.storage

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getStringFlow
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalSettingsApi::class)
class PreferencesStorage(private val settings: ObservableSettings) {
    var theme: String
        get() = settings[KEY_THEME] ?: DEFAULT_THEME
        set(value) { settings[KEY_THEME] = value }

    var language: String
        get() = settings[KEY_LANGUAGE] ?: DEFAULT_LANGUAGE
        set(value) { settings[KEY_LANGUAGE] = value }

    fun observeTheme(): Flow<String> =
        settings.getStringFlow(KEY_THEME, DEFAULT_THEME)

    fun observeLanguage(): Flow<String> =
        settings.getStringFlow(KEY_LANGUAGE, DEFAULT_LANGUAGE)

    companion object {
        private const val KEY_THEME = "pref_theme"
        private const val KEY_LANGUAGE = "pref_language"
        private const val DEFAULT_THEME = "system"
        private const val DEFAULT_LANGUAGE = "en"
    }
}
