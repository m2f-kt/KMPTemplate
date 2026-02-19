package com.m2f.template.localization

import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
import platform.Foundation.currentLocale
import platform.Foundation.languageCode

actual fun setAppLocale(languageTag: String) {
    NSUserDefaults.standardUserDefaults.setObject(listOf(languageTag), forKey = "AppleLanguages")
    NSUserDefaults.standardUserDefaults.synchronize()
}

actual fun getAppLocale(): String {
    val languages = NSUserDefaults.standardUserDefaults.objectForKey("AppleLanguages") as? List<*>
    return (languages?.firstOrNull() as? String)?.take(2) ?: NSLocale.currentLocale.languageCode
}
