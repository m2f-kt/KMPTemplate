package com.m2f.template.app.admin

import com.m2f.template.models.GroupRole

sealed interface RegisterMemberIntent {
    data class EmailChanged(val email: String) : RegisterMemberIntent
    data class PasswordChanged(val password: String) : RegisterMemberIntent
    data class FirstNameChanged(val firstName: String) : RegisterMemberIntent
    data class LastNameChanged(val lastName: String) : RegisterMemberIntent
    data class RoleChanged(val role: GroupRole) : RegisterMemberIntent
    data class SubmitRegisterMember(val groupId: String) : RegisterMemberIntent
}
