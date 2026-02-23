package com.m2f.template.app.auth

/**
 * Platform-specific check for invitation link parameters on app entry.
 *
 * - WASM: Checks `window.location` for `/invite/accept?token=...` path and query param
 * - Android: Returns null (deep link handled via intent)
 * - iOS: Returns null (universal link handled via app delegate)
 * - JVM Desktop: Returns null (not applicable)
 *
 * @return The invitation token if detected, null otherwise.
 */
expect fun checkInviteLink(): String?
