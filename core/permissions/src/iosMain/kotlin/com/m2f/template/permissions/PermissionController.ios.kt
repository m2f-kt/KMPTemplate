package com.m2f.template.permissions

import arrow.core.Either
import arrow.core.left
import com.m2f.template.models.AppError
import com.m2f.template.models.Permission
import com.m2f.template.models.PermissionStatus

/**
 * iOS `actual` for [PermissionController] — intentionally a minimal, honest no-op.
 *
 * Real iOS permission flows are framework- and main-thread-scoped and best driven from the
 * app/UI layer: e.g. `AVCaptureDevice.authorizationStatus(for:)` /
 * `requestAccess(for:completionHandler:)` for Microphone/Camera, `UNUserNotificationCenter`
 * for Notifications, and `CLLocationManager` (with its delegate callbacks) for Location.
 * Accessibility in the macOS-TCC sense has no iOS analogue. Wiring those — including the
 * delegate/callback lifecycles — is left to the consuming app rather than baked into this
 * context-free seam.
 *
 * Until the app provides that bridge:
 * - [status] reports [PermissionStatus.NotDetermined].
 * - [request] returns [AppError.Native.Unavailable].
 */
actual class PermissionController actual constructor() {

    actual fun status(permission: Permission): PermissionStatus = PermissionStatus.NotDetermined

    actual suspend fun request(permission: Permission): Either<AppError, Unit> =
        AppError.Native.Unavailable().left()
}
