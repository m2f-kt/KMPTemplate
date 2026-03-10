package com.m2f.template.app.profile

import com.m2f.template.models.UserTier
import com.m2f.template.models.localization.StringKey

sealed interface ProfileMutation {
    data class SetProfile(
        val userId: String,
        val email: String,
        val name: String,
        val tier: UserTier,
        val avatarUrl: String? = null,
    ) : ProfileMutation

    data class SetLoading(val loading: Boolean) : ProfileMutation
    data object StartEdit : ProfileMutation
    data object CancelEdit : ProfileMutation
    data class SetEditName(val name: String) : ProfileMutation
    data class SetEditEmail(val email: String) : ProfileMutation
    data class SetFieldErrors(val errors: Map<String, StringKey>) : ProfileMutation
    data class SetServerError(val error: StringKey?) : ProfileMutation
    data object SetSaveSuccess : ProfileMutation

    // Avatar mutations
    data class SetPendingImage(val bytes: ByteArray) : ProfileMutation {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as SetPendingImage
            if (!bytes.contentEquals(other.bytes)) return false
            return true
        }

        override fun hashCode(): Int = bytes.contentHashCode()
    }

    data object ShowCropDialog : ProfileMutation
    data object HideCropDialog : ProfileMutation
    data class SetUploadingAvatar(val uploading: Boolean) : ProfileMutation
    data class SetAvatarUrl(val url: String?) : ProfileMutation
}
