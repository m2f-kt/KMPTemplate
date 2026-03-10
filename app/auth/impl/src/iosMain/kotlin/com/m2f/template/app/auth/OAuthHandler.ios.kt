package com.m2f.template.app.auth

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

/**
 * iOS OAuth handler.
 * Opens the OAuth URL in Safari via UIApplication.
 * Callback returns via custom URL scheme `template://auth/callback`.
 */
actual class OAuthHandler actual constructor(private val serverBaseUrl: String) {
    actual fun startOAuth(provider: String) {
        val redirectUri = getRedirectUri()
        val url = "$serverBaseUrl/api/auth/oauth/$provider?state=$redirectUri"
        NSURL(string = url)?.let { nsUrl ->
            UIApplication.sharedApplication.openURL(nsUrl)
        }
    }

    actual fun getRedirectUri(): String = "template://auth/callback"
}
