package com.m2f.template.permissions

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.m2f.template.models.AppError
import com.m2f.template.models.Permission
import com.m2f.template.models.PermissionStatus
import com.m2f.template.platform.isMacOs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * JVM (desktop) `actual` for [PermissionController].
 *
 * Generalized from NoType's microphone gate + accessibility permission + read-only status
 * probe into a single [Permission]-dispatching controller:
 *
 * - **Microphone / Camera** → AVFoundation `AVCaptureDevice` (`authorizationStatusForMediaType:`
 *   for [status], `requestAccessForMediaType:` + bounded status-poll for [request]).
 * - **Accessibility** → `AXIsProcessTrusted()` (silent check; macOS has no programmatic prompt,
 *   so [request] points the user at System Settings via [openSystemSettings]).
 * - **Notifications / Location** → no generic desktop bridge here → [PermissionStatus.Unknown]
 *   / [AppError.Native.Unavailable].
 *
 * Off-macOS JVM hosts (Windows / Linux) have no TCC-style privacy database for these
 * permissions, so they degrade gracefully to [AppError.Native.Unavailable] /
 * [PermissionStatus.Unknown] rather than faking a grant. Every native call is gated on
 * [isMacOs] and wrapped so nothing throws across the boundary.
 */
actual class PermissionController actual constructor() {

    actual fun status(permission: Permission): PermissionStatus {
        if (!isMacOs()) return PermissionStatus.Unknown
        return when (permission) {
            Permission.Microphone -> deviceStatus(AvMediaType.AUDIO)
            Permission.Camera -> deviceStatus(AvMediaType.VIDEO)
            Permission.Accessibility -> accessibilityStatus()
            Permission.Notifications, Permission.Location -> PermissionStatus.Unknown
        }
    }

    actual suspend fun request(permission: Permission): Either<AppError, Unit> {
        if (!isMacOs()) return AppError.Native.Unavailable().left()
        return when (permission) {
            Permission.Microphone -> requestDevice(Permission.Microphone, AvMediaType.AUDIO)
            Permission.Camera -> requestDevice(Permission.Camera, AvMediaType.VIDEO)
            // macOS exposes no programmatic Accessibility prompt: report the silent status and
            // let the consuming UI route the user to System Settings via openSystemSettings().
            Permission.Accessibility ->
                permissionStatusToOutcome(Permission.Accessibility, accessibilityStatus())
            Permission.Notifications, Permission.Location -> AppError.Native.Unavailable().left()
        }
    }

    /** Silent, non-prompting AVFoundation device-authorization read mapped to a generic status. */
    private fun deviceStatus(mediaType: AvMediaType): PermissionStatus = try {
        osAuthorizationCodeToStatus(MacOsObjcBridge.authorizationStatus(mediaType))
    } catch (_: Throwable) {
        // UnsatisfiedLinkError / runtime symbol-resolution failure — never throw across the
        // boundary; report Unknown so the UI shows an honest indeterminate state.
        PermissionStatus.Unknown
    }

    /** Silent `AXIsProcessTrusted()` check; macOS only ever reports granted-or-not here. */
    private fun accessibilityStatus(): PermissionStatus = try {
        if (ApplicationServicesLib.INSTANCE.AXIsProcessTrusted()) {
            PermissionStatus.Granted
        } else {
            // Not trusted yet: surfaced as NotDetermined because the user can still grant it in
            // System Settings (there is no policy-restricted distinction from this API).
            PermissionStatus.NotDetermined
        }
    } catch (_: Throwable) {
        PermissionStatus.Unknown
    }

    /**
     * Prompting device flow. On an already-decided status returns immediately; on
     * [PermissionStatus.NotDetermined] fires the AVFoundation prompt then polls the status
     * (bounded by [REQUEST_TIMEOUT_MS]) until the user answers. Runs on [Dispatchers.IO] since
     * the poll sleeps while awaiting the user — it must not occupy a UI/Default thread.
     */
    private suspend fun requestDevice(
        permission: Permission,
        mediaType: AvMediaType
    ): Either<AppError, Unit> = withContext(Dispatchers.IO) {
        when (val current = deviceStatus(mediaType)) {
            PermissionStatus.NotDetermined -> promptAndAwait(permission, mediaType)
            else -> permissionStatusToOutcome(permission, current)
        }
    }

    private fun promptAndAwait(
        permission: Permission,
        mediaType: AvMediaType
    ): Either<AppError, Unit> {
        try {
            MacOsObjcBridge.requestAccess(mediaType)
        } catch (_: Throwable) {
            return AppError.Native.Unavailable().left()
        }
        val deadline = System.currentTimeMillis() + REQUEST_TIMEOUT_MS
        while (System.currentTimeMillis() < deadline) {
            when (val status = deviceStatus(mediaType)) {
                PermissionStatus.NotDetermined -> {
                    try {
                        TimeUnit.MILLISECONDS.sleep(POLL_INTERVAL_MS)
                    } catch (e: InterruptedException) {
                        // Restore the interrupt flag and stop polling — the caller is being
                        // cancelled; do not spin the rest of the timeout.
                        Thread.currentThread().interrupt()
                        return AppError.Permission.NotDetermined(permission).left()
                    }
                }
                else -> return permissionStatusToOutcome(permission, status)
            }
        }
        // Timed out waiting on the user — still undecided.
        return AppError.Permission.NotDetermined(permission).left()
    }

    private companion object {
        // Bounded wait on the NOT_DETERMINED prompt path so a wedged tccd cannot hang forever.
        const val REQUEST_TIMEOUT_MS: Long = 60_000L
        const val POLL_INTERVAL_MS: Long = 250L
    }
}

/**
 * macOS System Settings privacy panes that [openSystemSettings] can deep-link to, generalized
 * from NoType's single Accessibility anchor. Each maps to the documented
 * `x-apple.systempreferences:com.apple.preference.security?<anchor>` URL anchor.
 */
enum class SystemSettingsPane(val anchor: String) {
    PrivacyAccessibility("Privacy_Accessibility"),
    PrivacyMicrophone("Privacy_Microphone"),
    PrivacyCamera("Privacy_Camera"),
    PrivacyScreenCapture("Privacy_ScreenCapture")
}

/**
 * Opens the given macOS System Settings privacy [pane] via the documented
 * `x-apple.systempreferences:` URL scheme. `Desktop.browse(URI)` is the cross-platform JDK
 * entry point; on macOS it delegates to LaunchServices which resolves the URL-scheme handler
 * (System Settings.app).
 *
 * Non-macOS hosts and headless / sandboxed JVMs (where `Desktop.getDesktop().browse` throws
 * `UnsupportedOperationException`) degrade to [AppError.Native.Unavailable].
 */
fun openSystemSettings(pane: SystemSettingsPane): Either<AppError, Unit> {
    if (!isMacOs()) return AppError.Native.Unavailable().left()
    return try {
        val uri = java.net.URI(
            "x-apple.systempreferences:com.apple.preference.security?${pane.anchor}"
        )
        java.awt.Desktop.getDesktop().browse(uri)
        Unit.right()
    } catch (_: Throwable) {
        AppError.Native.Unavailable().left()
    }
}
