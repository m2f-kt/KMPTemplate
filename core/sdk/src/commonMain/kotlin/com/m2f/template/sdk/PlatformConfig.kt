package com.m2f.template.sdk

/**
 * Returns the default base URL for the API server on each platform.
 * Android emulator requires `10.0.2.2` to reach the host machine's localhost.
 */
expect fun defaultBaseUrl(): String
