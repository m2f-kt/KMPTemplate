package com.m2f.template.app.admin

import androidx.compose.runtime.Composable
import com.m2f.template.models.localization.StringKey
import org.jetbrains.compose.resources.stringResource
import template.app.admin.generated.resources.Res
import template.app.admin.generated.resources.error_ai_agent_failed
import template.app.admin.generated.resources.error_ai_agent_not_found
import template.app.admin.generated.resources.error_ai_conversation_not_found
import template.app.admin.generated.resources.error_ai_provider_unavailable
import template.app.admin.generated.resources.error_auth_invalid_credentials
import template.app.admin.generated.resources.error_auth_token_expired
import template.app.admin.generated.resources.error_auth_token_invalid
import template.app.admin.generated.resources.error_auth_unauthorized
import template.app.admin.generated.resources.error_auth_user_already_exists
import template.app.admin.generated.resources.error_client_network_error
import template.app.admin.generated.resources.error_client_timeout
import template.app.admin.generated.resources.error_client_unknown_error
import template.app.admin.generated.resources.error_generic
import template.app.admin.generated.resources.error_group_already_exists
import template.app.admin.generated.resources.error_group_forbidden
import template.app.admin.generated.resources.error_group_member_already_exists
import template.app.admin.generated.resources.error_group_not_found
import template.app.admin.generated.resources.error_server_internal_error
import template.app.admin.generated.resources.error_server_service_unavailable
import template.app.admin.generated.resources.error_user_forbidden
import template.app.admin.generated.resources.error_user_not_found
import template.app.admin.generated.resources.error_validation_email_blank
import template.app.admin.generated.resources.error_validation_email_invalid
import template.app.admin.generated.resources.error_validation_field_required
import template.app.admin.generated.resources.error_validation_invalid_field
import template.app.admin.generated.resources.error_validation_invalid_input
import template.app.admin.generated.resources.error_validation_missing_field
import template.app.admin.generated.resources.error_validation_name_blank
import template.app.admin.generated.resources.error_validation_name_length
import template.app.admin.generated.resources.error_validation_password_blank
import template.app.admin.generated.resources.error_validation_password_too_short
import template.app.admin.generated.resources.error_validation_passwords_mismatch
import template.app.admin.generated.resources.error_invitation_already_accepted
import template.app.admin.generated.resources.error_invitation_expired
import template.app.admin.generated.resources.error_invitation_not_found
import template.app.admin.generated.resources.error_invitation_revoked
import template.app.admin.generated.resources.error_invitation_email_mismatch
import template.app.admin.generated.resources.error_privacy_consent_required
import template.app.admin.generated.resources.error_privacy_deletion_pending
import template.app.admin.generated.resources.error_privacy_export_not_ready
import template.app.admin.generated.resources.error_validation_terms_not_accepted

/**
 * Composable bridge that resolves a [StringKey] to its localized string
 * using the admin module's own string resources.
 *
 * Exhaustive `when` ensures compile errors if new [StringKey] entries are added.
 */
@Composable
internal fun resolveStringKey(key: StringKey, vararg args: Any): String =
    when (key) {
        // Auth errors
        StringKey.AUTH_INVALID_CREDENTIALS -> stringResource(Res.string.error_auth_invalid_credentials, *args)
        StringKey.AUTH_TOKEN_EXPIRED -> stringResource(Res.string.error_auth_token_expired, *args)
        StringKey.AUTH_TOKEN_INVALID -> stringResource(Res.string.error_auth_token_invalid, *args)
        StringKey.AUTH_UNAUTHORIZED -> stringResource(Res.string.error_auth_unauthorized, *args)
        StringKey.AUTH_USER_ALREADY_EXISTS -> stringResource(Res.string.error_auth_user_already_exists, *args)

        // Validation errors (from AppError)
        StringKey.VALIDATION_INVALID_FIELD -> stringResource(Res.string.error_validation_invalid_field, *args)
        StringKey.VALIDATION_INVALID_INPUT -> stringResource(Res.string.error_validation_invalid_input, *args)
        StringKey.VALIDATION_MISSING_FIELD -> stringResource(Res.string.error_validation_missing_field, *args)

        // User errors
        StringKey.USER_NOT_FOUND -> stringResource(Res.string.error_user_not_found, *args)
        StringKey.USER_FORBIDDEN -> stringResource(Res.string.error_user_forbidden, *args)

        // Server errors
        StringKey.SERVER_INTERNAL_ERROR -> stringResource(Res.string.error_server_internal_error, *args)
        StringKey.SERVER_SERVICE_UNAVAILABLE -> stringResource(Res.string.error_server_service_unavailable, *args)

        // Client errors
        StringKey.CLIENT_NETWORK_ERROR -> stringResource(Res.string.error_client_network_error, *args)
        StringKey.CLIENT_TIMEOUT -> stringResource(Res.string.error_client_timeout, *args)
        StringKey.CLIENT_UNKNOWN_ERROR -> stringResource(Res.string.error_client_unknown_error, *args)

        // Group errors
        StringKey.GROUP_NOT_FOUND -> stringResource(Res.string.error_group_not_found, *args)
        StringKey.GROUP_FORBIDDEN -> stringResource(Res.string.error_group_forbidden, *args)
        StringKey.GROUP_ALREADY_EXISTS -> stringResource(Res.string.error_group_already_exists, *args)
        StringKey.GROUP_MEMBER_ALREADY_EXISTS -> stringResource(Res.string.error_group_member_already_exists, *args)

        // AI errors
        StringKey.AI_AGENT_FAILED -> stringResource(Res.string.error_ai_agent_failed, *args)
        StringKey.AI_AGENT_NOT_FOUND -> stringResource(Res.string.error_ai_agent_not_found, *args)
        StringKey.AI_CONVERSATION_NOT_FOUND -> stringResource(Res.string.error_ai_conversation_not_found, *args)
        StringKey.AI_PROVIDER_UNAVAILABLE -> stringResource(Res.string.error_ai_provider_unavailable, *args)

        // Validation messages
        StringKey.VALIDATION_EMAIL_BLANK -> stringResource(Res.string.error_validation_email_blank, *args)
        StringKey.VALIDATION_EMAIL_INVALID -> stringResource(Res.string.error_validation_email_invalid, *args)
        StringKey.VALIDATION_PASSWORD_TOO_SHORT -> stringResource(Res.string.error_validation_password_too_short, *args)
        StringKey.VALIDATION_PASSWORD_BLANK -> stringResource(Res.string.error_validation_password_blank, *args)
        StringKey.VALIDATION_NAME_BLANK -> stringResource(Res.string.error_validation_name_blank, *args)
        StringKey.VALIDATION_NAME_LENGTH -> stringResource(Res.string.error_validation_name_length, *args)
        StringKey.VALIDATION_FIELD_REQUIRED -> stringResource(Res.string.error_validation_field_required, *args)
        StringKey.VALIDATION_PASSWORDS_MISMATCH -> stringResource(Res.string.error_validation_passwords_mismatch, *args)
        StringKey.VALIDATION_TERMS_NOT_ACCEPTED -> stringResource(Res.string.error_validation_terms_not_accepted, *args)

        // Invitation errors
        StringKey.INVITATION_REVOKED -> stringResource(Res.string.error_invitation_revoked, *args)
        StringKey.INVITATION_EXPIRED -> stringResource(Res.string.error_invitation_expired, *args)
        StringKey.INVITATION_ALREADY_ACCEPTED -> stringResource(Res.string.error_invitation_already_accepted, *args)
        StringKey.INVITATION_NOT_FOUND -> stringResource(Res.string.error_invitation_not_found, *args)
        StringKey.INVITATION_EMAIL_MISMATCH -> stringResource(Res.string.error_invitation_email_mismatch, *args)

        // Privacy errors
        StringKey.PRIVACY_CONSENT_REQUIRED -> stringResource(Res.string.error_privacy_consent_required, *args)
        StringKey.PRIVACY_DELETION_PENDING -> stringResource(Res.string.error_privacy_deletion_pending, *args)
        StringKey.PRIVACY_EXPORT_NOT_READY -> stringResource(Res.string.error_privacy_export_not_ready, *args)

        // Generic fallback
        StringKey.GENERIC_ERROR -> stringResource(Res.string.error_generic, *args)
    }
