package com.m2f.template.app.admin

import com.m2f.template.models.GroupRole
import com.m2f.template.models.localization.StringKey

sealed interface RegisterMemberMutation {
    data class SetEmail(val email: String) : RegisterMemberMutation
    data class SetPassword(val password: String) : RegisterMemberMutation
    data class SetFirstName(val firstName: String) : RegisterMemberMutation
    data class SetLastName(val lastName: String) : RegisterMemberMutation
    data class SetRole(val role: GroupRole) : RegisterMemberMutation
    data class SetLoading(val loading: Boolean) : RegisterMemberMutation
    data class SetFieldErrors(val errors: Map<String, StringKey>) : RegisterMemberMutation
    data class SetServerError(val error: StringKey) : RegisterMemberMutation
}
