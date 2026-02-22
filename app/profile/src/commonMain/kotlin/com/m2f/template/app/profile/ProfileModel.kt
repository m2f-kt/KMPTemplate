package com.m2f.template.app.profile

import com.m2f.template.models.UserTier
import com.m2f.template.models.localization.StringKey

data class ProfileModel(
    val userId: String = "",
    val email: String = "",
    val name: String = "",
    val tier: UserTier = UserTier.Free,
    val avatarUrl: String? = null,
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val editName: String = "",
    val editEmail: String = "",
    val fieldErrors: Map<String, StringKey> = emptyMap(),
    val serverError: StringKey? = null,
    val saveSuccess: Boolean = false,
    val isUploadingAvatar: Boolean = false,
    val showCropDialog: Boolean = false,
    val pendingImageBytes: ByteArray? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ProfileModel
        if (userId != other.userId) return false
        if (email != other.email) return false
        if (name != other.name) return false
        if (tier != other.tier) return false
        if (avatarUrl != other.avatarUrl) return false
        if (isLoading != other.isLoading) return false
        if (isEditing != other.isEditing) return false
        if (editName != other.editName) return false
        if (editEmail != other.editEmail) return false
        if (fieldErrors != other.fieldErrors) return false
        if (serverError != other.serverError) return false
        if (saveSuccess != other.saveSuccess) return false
        if (isUploadingAvatar != other.isUploadingAvatar) return false
        if (showCropDialog != other.showCropDialog) return false
        if (pendingImageBytes != null) {
            if (other.pendingImageBytes == null) return false
            if (!pendingImageBytes.contentEquals(other.pendingImageBytes)) return false
        } else if (other.pendingImageBytes != null) return false
        return true
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + tier.hashCode()
        result = 31 * result + (avatarUrl?.hashCode() ?: 0)
        result = 31 * result + isLoading.hashCode()
        result = 31 * result + isEditing.hashCode()
        result = 31 * result + editName.hashCode()
        result = 31 * result + editEmail.hashCode()
        result = 31 * result + fieldErrors.hashCode()
        result = 31 * result + (serverError?.hashCode() ?: 0)
        result = 31 * result + saveSuccess.hashCode()
        result = 31 * result + isUploadingAvatar.hashCode()
        result = 31 * result + showCropDialog.hashCode()
        result = 31 * result + (pendingImageBytes?.contentHashCode() ?: 0)
        return result
    }
}
