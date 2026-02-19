package com.m2f.template.app.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.m2f.template.designsystem.components.TerminalText
import com.m2f.template.designsystem.components.button.TerminalButton
import com.m2f.template.designsystem.components.feedback.BadgeVariant
import com.m2f.template.designsystem.components.feedback.TerminalBadge
import com.m2f.template.designsystem.components.input.TerminalInput
import com.m2f.template.designsystem.components.input.TerminalPasswordInput
import com.m2f.template.designsystem.theme.TerminalTheme
import com.m2f.template.models.GroupRole

/**
 * Stateless register-member form screen.
 *
 * Displays form fields for email, password, first name, last name, and role selector.
 * Per-field validation errors are shown below each input.
 * Server errors are shown as a danger badge.
 *
 * @param state Current form state with field values, errors, and loading flag.
 * @param onEmailChange Callback when email field changes.
 * @param onPasswordChange Callback when password field changes.
 * @param onFirstNameChange Callback when first name field changes.
 * @param onLastNameChange Callback when last name field changes.
 * @param onRoleChange Callback when role selection changes.
 * @param onSubmit Callback to submit the registration form.
 * @param onBack Callback to navigate back.
 * @param modifier Modifier for the screen root.
 */
@Composable
fun RegisterMemberScreen(
    state: RegisterMemberModel,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onRoleChange: (GroupRole) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Top bar: back button + title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TerminalButton(
                text = "<-",
                onClick = onBack,
            )
            TerminalText(
                text = "> register_member",
                style = typography.xxl.copy(fontWeight = FontWeight.Bold),
                color = colors.text,
            )
        }

        // First Name
        val firstNameError = state.fieldErrors["firstName"]
        TerminalInput(
            value = state.firstName,
            onValueChange = onFirstNameChange,
            label = "first_name",
            placeholder = "John",
            isError = firstNameError != null,
            errorMessage = firstNameError,
        )

        // Last Name
        val lastNameError = state.fieldErrors["lastName"]
        TerminalInput(
            value = state.lastName,
            onValueChange = onLastNameChange,
            label = "last_name",
            placeholder = "Doe",
            isError = lastNameError != null,
            errorMessage = lastNameError,
        )

        // Email
        val emailError = state.fieldErrors["email"]
        TerminalInput(
            value = state.email,
            onValueChange = onEmailChange,
            label = "email",
            placeholder = "user@example.com",
            isError = emailError != null,
            errorMessage = emailError,
        )

        // Password
        val passwordError = state.fieldErrors["password"]
        TerminalPasswordInput(
            value = state.password,
            onValueChange = onPasswordChange,
            label = "password",
            placeholder = "min. 8 characters",
            isError = passwordError != null,
            errorMessage = passwordError,
        )

        // Role selector
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TerminalText(
                text = "role",
                style = typography.sm.copy(fontWeight = FontWeight.Medium),
                color = colors.textMuted,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TerminalBadge(
                    text = "Member",
                    variant = if (state.role is GroupRole.Member) BadgeVariant.Success else BadgeVariant.Default,
                    modifier = Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) { onRoleChange(GroupRole.Member) },
                )
                TerminalBadge(
                    text = "Admin",
                    variant = if (state.role is GroupRole.Admin) BadgeVariant.Success else BadgeVariant.Default,
                    modifier = Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) { onRoleChange(GroupRole.Admin) },
                )
            }
        }

        // Server error
        if (state.serverError != null) {
            TerminalBadge(
                text = state.serverError,
                variant = BadgeVariant.Error,
            )
        }

        // Submit button
        TerminalButton(
            text = if (state.isLoading) "$ registering..." else "register",
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
        )
    }
}
