package com.m2f.server.auth.routes

import com.m2f.core.config.server.conduit
import com.m2f.core.config.server.conduitAuth
import com.m2f.core.config.server.getModel
import com.m2f.server.auth.service.AuthService
import com.m2f.server.auth.service.PasswordResetService
import com.m2f.template.models.dto.ForgotPasswordRequest
import com.m2f.template.models.dto.LoginRequest
import com.m2f.template.models.dto.RefreshTokenRequest
import com.m2f.template.models.dto.RegisterRequest
import com.m2f.template.models.dto.ResetPasswordRequest
import com.m2f.template.models.routes.Auth
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.resources.post
import io.ktor.server.routing.Route

/**
 * Auth routes for registration, login, token refresh, logout, and password reset.
 * Uses type-safe @Resource handlers via Ktor Resources.
 */
fun Route.authRoutes(authService: AuthService, passwordResetService: PasswordResetService) {
    post<Auth.Register> {
        conduit(HttpStatusCode.Created) {
            val request = getModel<RegisterRequest>()
            authService.register(request)
        }
    }
    post<Auth.Login> {
        conduit {
            val request = getModel<LoginRequest>()
            authService.login(request)
        }
    }
    post<Auth.Refresh> {
        conduit {
            val request = getModel<RefreshTokenRequest>()
            authService.refresh(request)
        }
    }
    post<Auth.ForgotPassword> {
        conduit {
            val request = getModel<ForgotPasswordRequest>()
            passwordResetService.forgotPassword(request)
        }
    }
    post<Auth.ResetPassword> {
        conduit {
            val request = getModel<ResetPasswordRequest>()
            passwordResetService.resetPassword(request)
        }
    }
    authenticate {
        post<Auth.Logout> {
            conduitAuth { userId ->
                authService.logout(userId)
            }
        }
    }
}
