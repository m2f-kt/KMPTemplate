package com.m2f.template.app.auth

import android.content.Intent
import android.net.Uri

/**
 * Android OAuth handler.
 * Opens the OAuth URL via an ACTION_VIEW intent (launches default browser).
 * Callback returns via deep link `template://auth/callback`.
 *
 * Requires an intent filter in AndroidManifest.xml for the custom scheme.
 */
actual class OAuthHandler actual constructor(private val serverBaseUrl: String) {

    private var context: android.content.Context? = null

    /**
     * Attach an Android context for launching the browser intent.
     * Must be called before [startOAuth] (typically with Activity context).
     */
    fun attach(context: android.content.Context) {
        this.context = context
    }

    actual fun startOAuth(provider: String) {
        val redirectUri = getRedirectUri()
        val url = "$serverBaseUrl/api/auth/oauth/$provider?state=$redirectUri"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context?.startActivity(intent)
    }

    actual fun getRedirectUri(): String = "template://auth/callback"
}
