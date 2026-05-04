package com.m2f.template.sdk

/**
 * Returns the default base URL for the API server on each platform.
 * Android emulator requires `10.0.2.2` to reach the host machine's localhost.
 *
 * The port comes from the generated [DEFAULT_DEV_PORT] constant, which is
 * read from the project's `.env` (PORT) at Gradle build time. Edit `.env`
 * and rebuild — do not hardcode the port here.
 */
expect fun defaultBaseUrl(): String
