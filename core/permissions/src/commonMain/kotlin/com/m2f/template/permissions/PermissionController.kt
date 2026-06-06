package com.m2f.template.permissions

import arrow.core.Either
import com.m2f.template.models.AppError
import com.m2f.template.models.Permission
import com.m2f.template.models.PermissionStatus

/**
 * Generic, multiplatform permission seam built around the "prompt-once vs. status-probe"
 * distinction that platform privacy systems (macOS TCC, iOS, Android runtime permissions)
 * all share:
 *
 * - [request] is the **prompting** path. It may show the OS consent dialog (on a platform
 *   that supports it and only when the permission has not yet been decided) and resolves to
 *   the user's terminal decision. Call it at the moment the feature is first used — never on
 *   a hot/display path, since it can block while awaiting the user.
 * - [status] is the **read-only probe**. It NEVER shows a prompt, never blocks, and never
 *   mutates the OS privacy database. It is safe to call on every screen entry to reflect the
 *   current grant in the UI.
 *
 * This is the generalization of NoType's microphone/accessibility gates: the mic-specific
 * `MicrophonePermissionGate.ensureGranted()` and the accessibility `AccessibilityPermission`
 * collapse into a single [Permission]-parameterized controller, and the read-only
 * `MicrophonePermissionStatus.isGranted()` probe becomes [status].
 *
 * Actuals:
 *  - **jvm (desktop)**: macOS-backed via a self-contained AVFoundation / ApplicationServices
 *    ObjC JNA bridge. Microphone/Camera use `AVCaptureDevice`; Accessibility uses
 *    `AXIsProcessTrusted`. Off-macOS hosts degrade gracefully to
 *    [AppError.Native.Unavailable] / [PermissionStatus.Unknown].
 *  - **android / ios**: honest minimal actuals — real runtime-permission flows are
 *    Activity/app-scoped and are wired by the consuming app (see each actual's KDoc).
 *  - **wasmJs**: no-op (browser has no generic native permission bridge here).
 */
expect class PermissionController() {
    /**
     * Prompting path: ensures the platform has (or obtains) a grant for [permission].
     *
     * @return `Right(Unit)` when the permission is usable; a typed `Left` otherwise —
     *   [AppError.Permission.Denied] / [AppError.Permission.Restricted] /
     *   [AppError.Permission.NotDetermined] when the OS reports a decision, or
     *   [AppError.Native.Unavailable] when no native flow exists on this platform.
     */
    suspend fun request(permission: Permission): Either<AppError, Unit>

    /**
     * Read-only, non-prompting probe of the current [PermissionStatus] for [permission].
     * Must never throw, never block, and never mutate OS privacy state.
     */
    fun status(permission: Permission): PermissionStatus
}

/**
 * Canonical OS authorization codes shared by the platforms that model permission grants as a
 * small ordinal enum (notably Apple's `AVAuthorizationStatus`, where the values are public
 * and stable):
 *
 * - `0` → not yet asked (a prompt would be shown).
 * - `1` → restricted by policy (parental controls / MDM); no prompt.
 * - `2` → explicitly denied; the OS will not re-prompt.
 * - `3` → authorized / granted.
 *
 * Any other value is treated as indeterminate.
 */
object OsAuthorizationCode {
    const val NOT_DETERMINED: Int = 0
    const val RESTRICTED: Int = 1
    const val DENIED: Int = 2
    const val AUTHORIZED: Int = 3
}

/**
 * PURE, platform-independent mapping from an OS authorization code (see [OsAuthorizationCode])
 * to a generic [PermissionStatus]. Kept in commonMain and free of any native call so the full
 * branch matrix is unit-testable without resolving a single native symbol or touching host
 * privacy state.
 *
 * - [OsAuthorizationCode.AUTHORIZED]      → [PermissionStatus.Granted]
 * - [OsAuthorizationCode.DENIED]          → [PermissionStatus.Denied]
 * - [OsAuthorizationCode.RESTRICTED]      → [PermissionStatus.Restricted]
 * - [OsAuthorizationCode.NOT_DETERMINED]  → [PermissionStatus.NotDetermined]
 * - anything else                          → [PermissionStatus.Unknown]
 */
fun osAuthorizationCodeToStatus(code: Int): PermissionStatus = when (code) {
    OsAuthorizationCode.AUTHORIZED -> PermissionStatus.Granted
    OsAuthorizationCode.DENIED -> PermissionStatus.Denied
    OsAuthorizationCode.RESTRICTED -> PermissionStatus.Restricted
    OsAuthorizationCode.NOT_DETERMINED -> PermissionStatus.NotDetermined
    else -> PermissionStatus.Unknown
}

/**
 * PURE mapping from a probed [PermissionStatus] to the `request()` terminal outcome for a
 * given [permission]. Centralizes the "which statuses block / which proceed" policy so each
 * platform actual reuses identical, unit-testable logic:
 *
 * - [PermissionStatus.Granted] → `Right(Unit)`.
 * - [PermissionStatus.Denied] → `Left(AppError.Permission.Denied)`.
 * - [PermissionStatus.Restricted] → `Left(AppError.Permission.Restricted)`.
 * - [PermissionStatus.NotDetermined] → `Left(AppError.Permission.NotDetermined)` — the caller
 *   that owns a prompting capability should first attempt the prompt and only fall here if the
 *   status is still undecided afterwards.
 * - [PermissionStatus.Unknown] → `Left(AppError.Native.Unavailable)` — the platform could not
 *   determine the state, so no native flow is available to satisfy the request.
 */
fun permissionStatusToOutcome(
    permission: Permission,
    status: PermissionStatus
): Either<AppError, Unit> = when (status) {
    PermissionStatus.Granted -> Either.Right(Unit)
    PermissionStatus.Denied -> Either.Left(AppError.Permission.Denied(permission))
    PermissionStatus.Restricted -> Either.Left(AppError.Permission.Restricted(permission))
    PermissionStatus.NotDetermined -> Either.Left(AppError.Permission.NotDetermined(permission))
    PermissionStatus.Unknown -> Either.Left(AppError.Native.Unavailable())
}
