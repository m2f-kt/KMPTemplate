package com.m2f.template.storage

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set

class TokenStorage(private val settings: Settings) {

    private var sessionOnly: Boolean = false

    fun getAccessToken(): String? = settings[KEY_ACCESS_TOKEN]
    fun getRefreshToken(): String? = settings[KEY_REFRESH_TOKEN]

    fun saveTokens(accessToken: String, refreshToken: String, rememberMe: Boolean = true) {
        settings[KEY_ACCESS_TOKEN] = accessToken
        settings[KEY_REFRESH_TOKEN] = refreshToken
        settings[KEY_SESSION_ONLY] = !rememberMe
        sessionOnly = !rememberMe
    }

    fun clearTokens() {
        settings.remove(KEY_ACCESS_TOKEN)
        settings.remove(KEY_REFRESH_TOKEN)
        settings.remove(KEY_SESSION_ONLY)
        sessionOnly = false
    }

    fun isSessionOnly(): Boolean = settings.getBoolean(KEY_SESSION_ONLY, false)

    fun clearSessionTokens() {
        if (isSessionOnly()) {
            clearTokens()
        }
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "auth_access_token"
        private const val KEY_REFRESH_TOKEN = "auth_refresh_token"
        private const val KEY_SESSION_ONLY = "auth_session_only"
    }
}
