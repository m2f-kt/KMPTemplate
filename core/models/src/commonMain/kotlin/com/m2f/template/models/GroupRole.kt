package com.m2f.template.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Sealed class representing group-level membership roles.
 * Used for authorization within a group (separate from system-level [UserRole]).
 *
 * A user could be ADMIN in one group and MEMBER in another.
 *
 * Serializes to/from a flat string on the wire via [GroupRoleSerializer]:
 * `"role":"ADMIN"` (NOT `"role":{"type":"ADMIN"}`).
 */
@Serializable(with = GroupRoleSerializer::class)
sealed class GroupRole {
    abstract val value: String
    abstract val level: Int

    data object Owner : GroupRole() {
        override val value: String = "OWNER"
        override val level: Int = 2
    }

    data object Admin : GroupRole() {
        override val value: String = "ADMIN"
        override val level: Int = 1
    }

    data object Member : GroupRole() {
        override val value: String = "MEMBER"
        override val level: Int = 0
    }

    companion object {
        /**
         * Maps a role string (case-insensitive) to the corresponding [GroupRole].
         * Defaults to [Member] for unrecognized roles.
         */
        fun fromString(role: String): GroupRole = when (role.uppercase()) {
            "OWNER" -> Owner
            "ADMIN" -> Admin
            "MEMBER" -> Member
            else -> Member
        }

        val entries: List<GroupRole> = listOf(Owner, Admin, Member)
    }
}

/**
 * Custom KSerializer that serializes [GroupRole] as a flat string.
 * Ensures the wire format is `"OWNER"` instead of a nested polymorphic object.
 */
internal object GroupRoleSerializer : KSerializer<GroupRole> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("GroupRole", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: GroupRole) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): GroupRole {
        return GroupRole.fromString(decoder.decodeString())
    }
}
