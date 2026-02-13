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
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

/**
 * Auth routes for registration, login, token refresh, logout, and password reset.
 */
fun Route.authRoutes(authService: AuthService, passwordResetService: PasswordResetService) {
    route("/api/auth") {
        post("/register") {
            conduit(HttpStatusCode.Created) {
                val request = getModel<RegisterRequest>()
                authService.register(request)
            }
        }
        post("/login") {
            conduit {
                val request = getModel<LoginRequest>()
                authService.login(request)
            }
        }
        post("/refresh") {
            conduit {
                val request = getModel<RefreshTokenRequest>()
                authService.refresh(request)
            }
        }
        post("/forgot-password") {
            conduit {
                val request = getModel<ForgotPasswordRequest>()
                passwordResetService.forgotPassword(request)
            }
        }
        post("/reset-password") {
            conduit {
                val request = getModel<ResetPasswordRequest>()
                passwordResetService.resetPassword(request)
            }
        }
        authenticate {
            post("/logout") {
                conduitAuth { userId ->
                    authService.logout(userId)
                }
            }
        }
    }
}
