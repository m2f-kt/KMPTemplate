package com.m2f.server.auth.routes

import com.m2f.core.config.server.conduit
import com.m2f.core.config.server.getModel
import com.m2f.server.auth.service.AuthService
import com.m2f.template.models.dto.RegisterRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

/**
 * Auth routes for the registration endpoint.
 * Login, refresh, and logout routes will be added in Plan 02-02.
 */
fun Route.authRoutes(authService: AuthService) {
    route("/api/auth") {
        post("/register") {
            conduit(HttpStatusCode.Created) {
                val request = getModel<RegisterRequest>()
                authService.register(request)
            }
        }
    }
}
