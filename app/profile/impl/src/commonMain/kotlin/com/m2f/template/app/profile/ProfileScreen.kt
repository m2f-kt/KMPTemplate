package com.m2f.template.app.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.m2f.template.app.profile.tier.AdminTierContent
import com.m2f.template.app.profile.tier.FreeTierContent
import com.m2f.template.app.profile.tier.PaidTierContent
import com.m2f.template.app.profile.tier.PowerAdminTierContent
import com.m2f.template.app.profile.tier.PremiumTierContent
import com.m2f.template.designsystem.components.TerminalText
import com.m2f.template.designsystem.components.button.ButtonVariant
import com.m2f.template.designsystem.components.button.TerminalButton
import com.m2f.template.designsystem.components.card.TerminalCard
import com.m2f.template.designsystem.components.display.TerminalAvatar
import com.m2f.template.designsystem.components.feedback.AlertVariant
import com.m2f.template.designsystem.components.feedback.TerminalAlert
import com.m2f.template.designsystem.components.feedback.TerminalProgress
import com.m2f.template.designsystem.components.input.TerminalInput
import com.m2f.template.designsystem.components.picker.ImagePickerResult
import com.m2f.template.designsystem.components.picker.rememberImagePickerLauncher
import com.m2f.template.designsystem.theme.TerminalTheme
import com.m2f.template.designsystem.util.rememberDecodedImage
import com.m2f.template.models.UserTier
import org.jetbrains.compose.resources.stringResource
import template.app.profile.generated.resources.Res
import template.app.profile.generated.resources.profile_account_info
import template.app.profile.generated.resources.profile_account_info_subtitle
import template.app.profile.generated.resources.profile_avatar_tap_hint
import template.app.profile.generated.resources.profile_avatar_uploading
import template.app.profile.generated.resources.profile_back
import template.app.profile.generated.resources.profile_cancel_button
import template.app.profile.generated.resources.profile_command
import template.app.profile.generated.resources.profile_crop_cancel
import template.app.profile.generated.resources.profile_crop_confirm
import template.app.profile.generated.resources.profile_crop_dialog_desc
import template.app.profile.generated.resources.profile_crop_dialog_title
import template.app.profile.generated.resources.profile_edit_button
import template.app.profile.generated.resources.profile_edit_email_placeholder
import template.app.profile.generated.resources.profile_edit_name_placeholder
import template.app.profile.generated.resources.profile_edit_subtitle
import template.app.profile.generated.resources.profile_edit_title
import template.app.profile.generated.resources.profile_email_label
import template.app.profile.generated.resources.profile_error_title
import template.app.profile.generated.resources.profile_logout
import template.app.profile.generated.resources.profile_name_label
import template.app.profile.generated.resources.profile_save_button
import template.app.profile.generated.resources.profile_save_success
import template.app.profile.generated.resources.profile_save_success_title
import template.app.profile.generated.resources.profile_tier_label
import template.app.profile.generated.resources.profile_user_id_label

/**
 * Responsive profile screen with tier-aware content.
 *
 * Desktop (>840dp): Row with [ProfileSidebar] (260dp) + main content.
 * Mobile: Column with back button header + content.
 *
 * Uses `when (state.tier)` exhaustive match to render the appropriate
 * tier-specific content composable.
 *
 * @param state The current profile state.
 * @param onStartEditing Callback to enter edit mode.
 * @param onCancelEditing Callback to cancel editing.
 * @param onEditNameChange Callback when edit name changes.
 * @param onEditEmailChange Callback when edit email changes.
 * @param onSaveProfile Callback to save edited profile.
 * @param onLogout Callback to trigger logout.
 * @param onBack Callback to navigate back.
 * @param onImageSelected Callback when an image is selected from picker.
 * @param onCropConfirmed Callback when crop is confirmed.
 * @param onCropCancelled Callback when crop is cancelled.
 * @param modifier Modifier for the screen root.
 */
@Composable
fun ProfileScreen(
    state: ProfileModel,
    onStartEditing: () -> Unit,
    onCancelEditing: () -> Unit,
    onEditNameChange: (String) -> Unit,
    onEditEmailChange: (String) -> Unit,
    onSaveProfile: () -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit,
    onImageSelected: (ByteArray, String) -> Unit,
    onCropConfirmed: () -> Unit,
    onCropCancelled: () -> Unit,
    onNavigateToPrivacy: () -> Unit = {},
    modifier: Modifier = Modifier,
    localeSelector: (@Composable () -> Unit)? = null,
    privacyContent: (@Composable () -> Unit)? = null,
) {
    val colors = TerminalTheme.colors

    val launchImagePicker = rememberImagePickerLauncher { result: ImagePickerResult? ->
        if (result != null) {
            onImageSelected(result.bytes, result.mimeType)
        }
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize().background(colors.bg),
    ) {
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    TerminalProgress()
                }
            }
            maxWidth > 840.dp -> {
                DesktopProfile(
                    state = state,
                    onStartEditing = onStartEditing,
                    onCancelEditing = onCancelEditing,
                    onEditNameChange = onEditNameChange,
                    onEditEmailChange = onEditEmailChange,
                    onSaveProfile = onSaveProfile,
                    onLogout = onLogout,
                    onBack = onBack,
                    onAvatarClick = launchImagePicker,
                    onNavigateToPrivacy = onNavigateToPrivacy,
                    localeSelector = localeSelector,
                    privacyContent = privacyContent,
                )
            }
            else -> {
                MobileProfile(
                    state = state,
                    onStartEditing = onStartEditing,
                    onCancelEditing = onCancelEditing,
                    onEditNameChange = onEditNameChange,
                    onEditEmailChange = onEditEmailChange,
                    onSaveProfile = onSaveProfile,
                    onLogout = onLogout,
                    onBack = onBack,
                    onAvatarClick = launchImagePicker,
                    localeSelector = localeSelector,
                )
            }
        }

        // Crop Dialog Overlay
        if (state.showCropDialog) {
            CropDialog(
                imageBytes = state.pendingImageBytes,
                onConfirm = onCropConfirmed,
                onCancel = onCropCancelled,
            )
        }
    }
}

// -- Desktop Layout --

@Composable
private fun DesktopProfile(
    state: ProfileModel,
    onStartEditing: () -> Unit,
    onCancelEditing: () -> Unit,
    onEditNameChange: (String) -> Unit,
    onEditEmailChange: (String) -> Unit,
    onSaveProfile: () -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit,
    onAvatarClick: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    localeSelector: (@Composable () -> Unit)? = null,
    privacyContent: (@Composable () -> Unit)? = null,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    var selectedNavItem by remember { mutableStateOf("profile") }

    Row(modifier = Modifier.fillMaxSize()) {
        ProfileSidebar(
            tier = state.tier,
            userName = state.name.ifBlank { state.email },
            selectedItem = selectedNavItem,
            onNavItemSelected = { key ->
                if (key == "privacy" && privacyContent == null) {
                    onNavigateToPrivacy()
                } else {
                    selectedNavItem = key
                }
            },
            onLogout = onLogout,
        )

        when (selectedNavItem) {
            "privacy" -> if (privacyContent != null) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(32.dp),
                ) {
                    privacyContent()
                }
            }
            else -> Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                TerminalText(
                    text = stringResource(Res.string.profile_back),
                    style = typography.sm,
                    color = colors.textMuted,
                    modifier = Modifier.clickable(onClick = onBack),
                )

                ProfileHeader(
                    state = state,
                    onAvatarClick = onAvatarClick,
                )

                if (state.isEditing) {
                    EditProfileSection(
                        state = state,
                        onEditNameChange = onEditNameChange,
                        onEditEmailChange = onEditEmailChange,
                        onSaveProfile = onSaveProfile,
                        onCancelEditing = onCancelEditing,
                    )
                } else {
                    ProfileInfoCard(state = state, onStartEditing = onStartEditing)
                }

                if (localeSelector != null) {
                    localeSelector()
                }

                TierContent(state = state)
            }
        }
    }
}

// -- Mobile Layout --

@Composable
private fun MobileProfile(
    state: ProfileModel,
    onStartEditing: () -> Unit,
    onCancelEditing: () -> Unit,
    onEditNameChange: (String) -> Unit,
    onEditEmailChange: (String) -> Unit,
    onSaveProfile: () -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit,
    onAvatarClick: () -> Unit,
    localeSelector: (@Composable () -> Unit)? = null,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography
    val spacing = TerminalTheme.spacing

    Column(modifier = Modifier.fillMaxSize()) {
        // Back button header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .padding(horizontal = spacing.lg, vertical = spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TerminalText(
                text = stringResource(Res.string.profile_back),
                style = typography.sm,
                color = colors.textMuted,
                modifier = Modifier.clickable(onClick = onBack),
            )
            TerminalText(
                text = stringResource(Res.string.profile_logout),
                style = typography.sm,
                color = colors.textDim,
                modifier = Modifier.clickable(onClick = onLogout),
            )
        }

        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.border),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            ProfileHeader(
                state = state,
                onAvatarClick = onAvatarClick,
            )

            if (state.isEditing) {
                EditProfileSection(
                    state = state,
                    onEditNameChange = onEditNameChange,
                    onEditEmailChange = onEditEmailChange,
                    onSaveProfile = onSaveProfile,
                    onCancelEditing = onCancelEditing,
                )
            } else {
                ProfileInfoCard(state = state, onStartEditing = onStartEditing)
            }

            if (localeSelector != null) {
                localeSelector()
            }

            TierContent(state = state)
        }
    }
}

// -- Shared Components --

@Composable
private fun ProfileHeader(
    state: ProfileModel,
    onAvatarClick: () -> Unit,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Tappable avatar with upload progress indicator
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(50))
                .clickable(enabled = !state.isUploadingAvatar, onClick = onAvatarClick),
            contentAlignment = Alignment.Center,
        ) {
            val initials = state.name.takeIf { it.isNotBlank() }?.take(2)?.uppercase()
                ?: state.email.take(2).uppercase()
            TerminalAvatar(
                initials = initials,
                imageUrl = state.avatarUrl,
                size = 64.dp,
            )

            // Upload progress overlay
            if (state.isUploadingAvatar) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.bg.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center,
                ) {
                    TerminalProgress()
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            TerminalText(
                text = stringResource(Res.string.profile_command),
                style = typography.xxl.copy(fontWeight = FontWeight.Bold),
                color = colors.text,
            )
            TerminalText(
                text = "// ${state.tier.displayName} | ${state.email}",
                style = typography.sm,
                color = colors.textMuted,
            )
            if (!state.isUploadingAvatar) {
                TerminalText(
                    text = stringResource(Res.string.profile_avatar_tap_hint),
                    style = typography.xs,
                    color = colors.textDim,
                )
            } else {
                TerminalText(
                    text = stringResource(Res.string.profile_avatar_uploading),
                    style = typography.xs,
                    color = colors.accent,
                )
            }
        }
    }
}

@Composable
private fun ProfileInfoCard(
    state: ProfileModel,
    onStartEditing: () -> Unit,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    if (state.saveSuccess) {
        TerminalAlert(
            message = stringResource(Res.string.profile_save_success),
            variant = AlertVariant.Success,
            title = stringResource(Res.string.profile_save_success_title),
        )
        Spacer(modifier = Modifier.height(8.dp))
    }

    if (state.serverError != null) {
        TerminalAlert(
            message = resolveStringKey(state.serverError),
            variant = AlertVariant.Error,
            title = stringResource(Res.string.profile_error_title),
        )
        Spacer(modifier = Modifier.height(8.dp))
    }

    TerminalCard(
        title = stringResource(Res.string.profile_account_info),
        description = stringResource(Res.string.profile_account_info_subtitle),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            InfoRow(label = stringResource(Res.string.profile_name_label), value = state.name)
            InfoRow(label = stringResource(Res.string.profile_email_label), value = state.email)
            InfoRow(label = stringResource(Res.string.profile_user_id_label), value = state.userId)
            InfoRow(label = stringResource(Res.string.profile_tier_label), value = state.tier.displayName)

            Spacer(modifier = Modifier.height(4.dp))

            TerminalButton(
                text = stringResource(Res.string.profile_edit_button),
                onClick = onStartEditing,
                variant = ButtonVariant.Secondary,
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TerminalText(
            text = "$label:",
            style = typography.sm,
            color = colors.textMuted,
            modifier = Modifier.width(80.dp),
        )
        TerminalText(
            text = value,
            style = typography.sm,
            color = colors.text,
        )
    }
}

@Composable
private fun EditProfileSection(
    state: ProfileModel,
    onEditNameChange: (String) -> Unit,
    onEditEmailChange: (String) -> Unit,
    onSaveProfile: () -> Unit,
    onCancelEditing: () -> Unit,
) {
    TerminalCard(
        title = stringResource(Res.string.profile_edit_title),
        description = stringResource(Res.string.profile_edit_subtitle),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (state.serverError != null) {
                TerminalAlert(
                    message = resolveStringKey(state.serverError),
                    variant = AlertVariant.Error,
                    title = stringResource(Res.string.profile_error_title),
                )
            }

            TerminalInput(
                value = state.editName,
                onValueChange = onEditNameChange,
                label = stringResource(Res.string.profile_name_label),
                placeholder = stringResource(Res.string.profile_edit_name_placeholder),
                isError = state.fieldErrors.containsKey("name"),
                errorMessage = state.fieldErrors["name"]?.let { resolveStringKey(it) },
            )

            TerminalInput(
                value = state.editEmail,
                onValueChange = onEditEmailChange,
                label = stringResource(Res.string.profile_email_label),
                placeholder = stringResource(Res.string.profile_edit_email_placeholder),
                isError = state.fieldErrors.containsKey("email"),
                errorMessage = state.fieldErrors["email"]?.let { resolveStringKey(it) },
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TerminalButton(
                    text = stringResource(Res.string.profile_save_button),
                    onClick = onSaveProfile,
                    variant = ButtonVariant.Default,
                )
                TerminalButton(
                    text = stringResource(Res.string.profile_cancel_button),
                    onClick = onCancelEditing,
                    variant = ButtonVariant.Ghost,
                )
            }
        }
    }
}

@Composable
private fun TierContent(state: ProfileModel) {
    when (state.tier) {
        is UserTier.Free -> FreeTierContent(state = state)
        is UserTier.Paid -> PaidTierContent(state = state)
        is UserTier.Premium -> PremiumTierContent(state = state)
        is UserTier.Admin -> AdminTierContent(state = state)
        is UserTier.PowerAdmin -> PowerAdminTierContent(state = state)
    }
}

/**
 * Simple crop dialog overlay. Shows a circular preview of the selected image
 * with confirm/cancel buttons. In a full implementation, this would include
 * a crop region selector with drag handles.
 */
@Composable
private fun CropDialog(
    imageBytes: ByteArray?,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    // Decode image bytes to ImageBitmap using platform-specific decoder
    val imageBitmap = rememberDecodedImage(imageBytes)

    // Full screen overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg.copy(alpha = 0.9f))
            .clickable(enabled = false) { /* consume clicks */ },
        contentAlignment = Alignment.Center,
    ) {
        TerminalCard(
            title = stringResource(Res.string.profile_crop_dialog_title),
            description = stringResource(Res.string.profile_crop_dialog_desc),
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(0.8f),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                // Image preview (circular crop preview)
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(50))
                        .background(colors.surface),
                    contentAlignment = Alignment.Center,
                ) {
                    if (imageBitmap != null) {
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = "Image preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else if (imageBytes != null) {
                        // Fallback: show size if decoding failed
                        TerminalText(
                            text = "${imageBytes.size / 1024} KB",
                            style = typography.md,
                            color = colors.textMuted,
                        )
                    } else {
                        TerminalText(
                            text = "No image",
                            style = typography.sm,
                            color = colors.textMuted,
                        )
                    }
                }

                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    TerminalButton(
                        text = stringResource(Res.string.profile_crop_confirm),
                        onClick = onConfirm,
                        variant = ButtonVariant.Default,
                    )
                    TerminalButton(
                        text = stringResource(Res.string.profile_crop_cancel),
                        onClick = onCancel,
                        variant = ButtonVariant.Ghost,
                    )
                }
            }
        }
    }
}
