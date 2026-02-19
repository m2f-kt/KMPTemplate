package com.m2f.template.localization

import java.util.Locale

actual fun setAppLocale(languageTag: String) {
    Locale.setDefault(Locale.forLanguageTag(languageTag))
}

actual fun getAppLocale(): String = Locale.getDefault().language
