package com.m2f.template.localization

import java.util.Locale

actual fun setAppLocale(languageTag: String) {
    val locale = Locale.forLanguageTag(languageTag)
    Locale.setDefault(locale)
}

actual fun getAppLocale(): String = Locale.getDefault().language
