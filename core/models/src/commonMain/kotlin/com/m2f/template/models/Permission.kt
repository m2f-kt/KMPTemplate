package com.m2f.template.models

import kotlinx.serialization.Serializable

/**
 * Generic runtime permissions surfaced by platform/permission modules.
 *
 * Kept platform-agnostic in `core:models` so feature, SDK, and native bridge
 * modules can request and report permission state without depending on any
 * specific platform API.
 */
@Serializable
enum class Permission {
    Microphone,
    Camera,
    Accessibility,
    Notifications,
    Location
}

/**
 * Generic status of a [Permission] as reported by a platform bridge.
 *
 * Modeled as a sealed interface of data objects (mirroring the closed-state
 * style used elsewhere in this module) so `when` checks stay exhaustive.
 */
@Serializable
sealed interface PermissionStatus {
    /** The permission has been granted and is usable. */
    @Serializable
    data object Granted : PermissionStatus

    /** The user (or system) explicitly denied the permission. */
    @Serializable
    data object Denied : PermissionStatus

    /** The permission is unavailable due to policy/parental controls; not user-changeable. */
    @Serializable
    data object Restricted : PermissionStatus

    /** The permission has not yet been requested from the user. */
    @Serializable
    data object NotDetermined : PermissionStatus

    /** The platform could not determine the permission state. */
    @Serializable
    data object Unknown : PermissionStatus
}
