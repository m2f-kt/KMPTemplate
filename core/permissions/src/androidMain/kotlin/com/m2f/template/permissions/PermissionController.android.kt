package com.m2f.template.permissions

import arrow.core.Either
import arrow.core.left
import com.m2f.template.models.AppError
import com.m2f.template.models.Permission
import com.m2f.template.models.PermissionStatus

/**
 * Android `actual` for [PermissionController] — intentionally a minimal, honest no-op.
 *
 * Real Android runtime-permission flows are **Activity/app-scoped**: they require a `Context`
 * (for `ContextCompat.checkSelfPermission`) and an `Activity` / `ActivityResultLauncher` (for
 * `requestPermissions`, which delivers its result asynchronously through an Activity callback).
 * A platform-agnostic, context-free controller like this one cannot own that lifecycle without
 * leaking an Activity reference, so the real wiring is deliberately left to the consuming app.
 *
 * Until the app provides that bridge:
 * - [status] reports [PermissionStatus.NotDetermined] (honest "not probed here", never a faked
 *   grant).
 * - [request] returns [AppError.Native.Unavailable] (no native flow is available from this
 *   context-free seam).
 */
actual class PermissionController actual constructor() {

    actual fun status(permission: Permission): PermissionStatus = PermissionStatus.NotDetermined

    actual suspend fun request(permission: Permission): Either<AppError, Unit> =
        AppError.Native.Unavailable().left()
}
