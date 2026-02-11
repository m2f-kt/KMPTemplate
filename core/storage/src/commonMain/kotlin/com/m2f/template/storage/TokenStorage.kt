package com.m2f.template.storage

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set

class TokenStorage(private val settings: Settings) {
    fun getAccessToken(): String? = settings[KEY_ACCESS_TOKEN]
    fun getRefreshToken(): String? = settings[KEY_REFRESH_TOKEN]

    fun saveTokens(accessToken: String, refreshToken: String) {
        settings[KEY_ACCESS_TOKEN] = accessToken
        settings[KEY_REFRESH_TOKEN] = refreshToken
    }

    fun clearTokens() {
        settings.remove(KEY_ACCESS_TOKEN)
        settings.remove(KEY_REFRESH_TOKEN)
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "auth_access_token"
        private const val KEY_REFRESH_TOKEN = "auth_refresh_token"
    }
}
