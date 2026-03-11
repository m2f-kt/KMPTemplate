package com.m2f.server.auth.contract.service

import arrow.core.raise.Raise
import com.m2f.core.config.server.DomainError
import com.m2f.template.models.dto.AuthResponse
import com.m2f.template.models.dto.LoginRequest
import com.m2f.template.models.dto.RefreshTokenRequest
import com.m2f.template.models.dto.RegisterRequest

/**
 * Authentication service handling registration, login, token refresh, and logout.
 */
interface AuthService {
    context(raise: Raise<DomainError>)
    suspend fun register(request: RegisterRequest): AuthResponse

    context(raise: Raise<DomainError>)
    suspend fun login(request: LoginRequest): AuthResponse

    context(raise: Raise<DomainError>)
    suspend fun refresh(request: RefreshTokenRequest): AuthResponse

    context(raise: Raise<DomainError>)
    suspend fun logout(userId: String): Map<String, String>
}
