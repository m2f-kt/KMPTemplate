package com.m2f.template.permissions

import arrow.core.Either
import arrow.core.left
import com.m2f.template.models.AppError
import com.m2f.template.models.Permission
import com.m2f.template.models.PermissionStatus

/**
 * wasmJs `actual` for [PermissionController] — no-op.
 *
 * The browser exposes permission state through the asynchronous `navigator.permissions` /
 * `MediaDevices.getUserMedia` Web APIs, which are gesture- and Promise-scoped and do not map
 * onto the generic [Permission] set (Accessibility has no web analogue at all). Rather than
 * fake a grant, this seam stays honest and unavailable; any browser permission flow is left to
 * the consuming web app.
 *
 * - [status] reports [PermissionStatus.NotDetermined].
 * - [request] returns [AppError.Native.Unavailable].
 */
actual class PermissionController actual constructor() {

    actual fun status(permission: Permission): PermissionStatus = PermissionStatus.NotDetermined

    actual suspend fun request(permission: Permission): Either<AppError, Unit> =
        AppError.Native.Unavailable().left()
}
