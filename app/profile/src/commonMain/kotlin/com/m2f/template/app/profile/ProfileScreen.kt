package com.m2f.template.app.profile

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
import com.m2f.template.designsystem.components.feedback.AlertVariant
import com.m2f.template.designsystem.components.feedback.TerminalAlert
import com.m2f.template.designsystem.components.feedback.TerminalProgress
import com.m2f.template.designsystem.components.input.TerminalInput
import com.m2f.template.designsystem.theme.TerminalTheme
import com.m2f.template.models.UserTier

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
    modifier: Modifier = Modifier,
) {
    val colors = TerminalTheme.colors

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
                )
            }
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
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    var selectedNavItem by remember { mutableStateOf("profile") }

    Row(modifier = Modifier.fillMaxSize()) {
        ProfileSidebar(
            tier = state.tier,
            userName = state.name.ifBlank { state.email },
            selectedItem = selectedNavItem,
            onNavItemSelected = { selectedNavItem = it },
            onLogout = onLogout,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            TerminalText(
                text = "< back",
                style = typography.sm,
                color = colors.textMuted,
                modifier = Modifier.clickable(onClick = onBack),
            )

            ProfileHeader(state = state)

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

            TierContent(state = state)
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
                text = "< back",
                style = typography.sm,
                color = colors.textMuted,
                modifier = Modifier.clickable(onClick = onBack),
            )
            TerminalText(
                text = "$ logout",
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
            ProfileHeader(state = state)

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

            TierContent(state = state)
        }
    }
}

// -- Shared Components --

@Composable
private fun ProfileHeader(state: ProfileModel) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        TerminalText(
            text = "$ user_profile",
            style = typography.xxl.copy(fontWeight = FontWeight.Bold),
            color = colors.text,
        )
        TerminalText(
            text = "// ${state.tier.displayName} | ${state.email}",
            style = typography.sm,
            color = colors.textMuted,
        )
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
            message = "Profile updated successfully.",
            variant = AlertVariant.Success,
            title = "saved",
        )
        Spacer(modifier = Modifier.height(8.dp))
    }

    if (state.serverError != null) {
        TerminalAlert(
            message = state.serverError,
            variant = AlertVariant.Error,
            title = "error",
        )
        Spacer(modifier = Modifier.height(8.dp))
    }

    TerminalCard(
        title = "account_info",
        description = "// personal details",
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            InfoRow(label = "name", value = state.name)
            InfoRow(label = "email", value = state.email)
            InfoRow(label = "user_id", value = state.userId)
            InfoRow(label = "tier", value = state.tier.displayName)

            Spacer(modifier = Modifier.height(4.dp))

            TerminalButton(
                text = "> edit profile",
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
        title = "edit_profile",
        description = "// modify name and email",
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (state.serverError != null) {
                TerminalAlert(
                    message = state.serverError,
                    variant = AlertVariant.Error,
                    title = "error",
                )
            }

            TerminalInput(
                value = state.editName,
                onValueChange = onEditNameChange,
                label = "name",
                placeholder = "Enter your name",
                isError = state.fieldErrors.containsKey("name"),
                errorMessage = state.fieldErrors["name"],
            )

            TerminalInput(
                value = state.editEmail,
                onValueChange = onEditEmailChange,
                label = "email",
                placeholder = "Enter your email",
                isError = state.fieldErrors.containsKey("email"),
                errorMessage = state.fieldErrors["email"],
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TerminalButton(
                    text = "> save",
                    onClick = onSaveProfile,
                    variant = ButtonVariant.Default,
                )
                TerminalButton(
                    text = "cancel",
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
