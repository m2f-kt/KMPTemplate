package com.m2f.template.sdk

import java.io.File

/**
 * JVM-desktop base URL.
 *
 * Resolution order (dev-launch convenience; all loopback, so [requireSecureBaseUrl] is satisfied):
 *  1. `SERVER_BASE_URL` env — explicit override.
 *  2. The dev port-file `$TMPDIR/template-dev-server.url` written by `scripts/dev-run.sh` when it
 *     moves the backend off the default port (e.g. the default was occupied). This lets the
 *     hot-reload desktop app find the backend the launcher actually started, without recompiling.
 *  3. The `.env`-baked [DEFAULT_DEV_PORT] default.
 *
 * In a packaged/prod build neither the env nor the file is present, so it falls through to the
 * default exactly as before.
 */
actual fun defaultBaseUrl(): String {
    System.getenv("SERVER_BASE_URL")?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }

    val portFile = File(System.getProperty("java.io.tmpdir"), "template-dev-server.url")
    runCatching {
        if (portFile.isFile) {
            portFile.readText().trim().takeIf { it.isNotEmpty() }?.let { return it }
        }
    }

    return "http://localhost:$DEFAULT_DEV_PORT"
}
