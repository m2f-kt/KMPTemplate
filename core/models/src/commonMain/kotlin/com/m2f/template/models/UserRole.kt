package com.m2f.template.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Sealed class representing user role levels.
 * Used for authorization gating across both server and client.
 *
 * Serializes to/from a flat string on the wire via [UserRoleSerializer]:
 * `"role":"ADMIN"` (NOT `"role":{"type":"ADMIN"}`).
 * This preserves backward compatibility with the previous `role: String` format.
 */
@Serializable(with = UserRoleSerializer::class)
sealed class UserRole {
    abstract val value: String
    abstract val level: Int

    data object User : UserRole() {
        override val value: String = "USER"
        override val level: Int = 0
    }

    data object Admin : UserRole() {
        override val value: String = "ADMIN"
        override val level: Int = 1
    }

    data object PowerAdmin : UserRole() {
        override val value: String = "POWER_ADMIN"
        override val level: Int = 2
    }

    companion object {
        /**
         * Maps a role string (case-insensitive) to the corresponding [UserRole].
         * Defaults to [User] for unrecognized roles.
         */
        fun fromString(role: String): UserRole = when (role.uppercase()) {
            "USER" -> User
            "ADMIN" -> Admin
            "POWER_ADMIN", "POWERADMIN" -> PowerAdmin
            else -> User
        }

        val entries: List<UserRole> = listOf(User, Admin, PowerAdmin)
    }
}

/**
 * Custom KSerializer that serializes [UserRole] as a flat string.
 * Ensures the wire format is `"ADMIN"` instead of a nested polymorphic object.
 */
internal object UserRoleSerializer : KSerializer<UserRole> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("UserRole", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UserRole) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): UserRole {
        return UserRole.fromString(decoder.decodeString())
    }
}
