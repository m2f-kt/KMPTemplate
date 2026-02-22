package com.m2f.template.app.profile

sealed interface ProfileIntent {
    data object LoadProfile : ProfileIntent
    data object StartEditing : ProfileIntent
    data object CancelEditing : ProfileIntent
    data class EditNameChanged(val name: String) : ProfileIntent
    data class EditEmailChanged(val email: String) : ProfileIntent
    data object SaveProfileClicked : ProfileIntent
    data object LogoutClicked : ProfileIntent

    // Avatar intents
    data class ImageSelected(val bytes: ByteArray, val mimeType: String) : ProfileIntent {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as ImageSelected
            if (!bytes.contentEquals(other.bytes)) return false
            if (mimeType != other.mimeType) return false
            return true
        }

        override fun hashCode(): Int {
            var result = bytes.contentHashCode()
            result = 31 * result + mimeType.hashCode()
            return result
        }
    }

    data object CropConfirmed : ProfileIntent
    data object CropCancelled : ProfileIntent
}
