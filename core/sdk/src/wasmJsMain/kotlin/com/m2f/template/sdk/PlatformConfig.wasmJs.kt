package com.m2f.template.sdk

@JsFun("() => window.location.hostname")
private external fun browserHostname(): String

@JsFun("() => window.location.protocol")
private external fun browserProtocol(): String

/**
 * Derive the API base URL from the page's own hostname so that loading the
 * web app from a LAN IP (e.g. http://192.168.3.5:8080 on a phone) keeps the
 * API call on the same hostname instead of resolving to the phone's localhost.
 */
actual fun defaultBaseUrl(): String =
    "${browserProtocol()}//${browserHostname()}:$DEFAULT_DEV_PORT"
