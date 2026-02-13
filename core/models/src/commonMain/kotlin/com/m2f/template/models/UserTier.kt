package com.m2f.template.models

import kotlinx.serialization.Serializable

/**
 * Sealed class representing user tier levels.
 * Used for profile differentiation and feature gating.
 *
 * Wire format stays as [String] in [com.m2f.template.models.dto.UserResponse.role].
 * Client-side code uses the sealed type via the [com.m2f.template.models.dto.tier] extension.
 */
@Serializable
sealed class UserTier {
    abstract val displayName: String
    abstract val level: Int

    @Serializable
    data object Free : UserTier() {
        override val displayName: String = "free_tier"
        override val level: Int = 0
    }

    @Serializable
    data object Paid : UserTier() {
        override val displayName: String = "paid_user"
        override val level: Int = 1
    }

    @Serializable
    data object Premium : UserTier() {
        override val displayName: String = "premium_user"
        override val level: Int = 2
    }

    @Serializable
    data object Admin : UserTier() {
        override val displayName: String = "admin_user"
        override val level: Int = 3
    }

    @Serializable
    data object PowerAdmin : UserTier() {
        override val displayName: String = "power_admin"
        override val level: Int = 4
    }

    companion object {
        /**
         * Maps a role string (case-insensitive) to the corresponding [UserTier].
         * Defaults to [Free] for unrecognized roles.
         */
        fun fromString(role: String): UserTier = when (role.uppercase()) {
            "FREE" -> Free
            "PAID" -> Paid
            "PREMIUM" -> Premium
            "ADMIN" -> Admin
            "POWERADMIN", "POWER_ADMIN" -> PowerAdmin
            else -> Free
        }
    }
}
