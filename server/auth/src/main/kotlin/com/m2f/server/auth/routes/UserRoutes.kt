package com.m2f.server.auth.routes

import com.m2f.core.config.server.conduitAuth
import com.m2f.core.config.server.getModel
import com.m2f.core.config.server.getStringParam
import com.m2f.server.auth.authorization.withRole
import com.m2f.server.auth.service.UserService
import com.m2f.template.models.UserRole
import com.m2f.template.models.dto.UpdateProfileRequest
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route

/**
 * User profile management routes.
 * All routes require JWT authentication.
 * Admin-only routes are additionally protected by the RBAC plugin.
 */
fun Route.userRoutes(userService: UserService) {
    authenticate {
        route("/api/users") {
            // Get own profile
            get("/me") {
                conduitAuth { userId ->
                    userService.getProfile(userId)
                }
            }

            // Update own profile
            put("/me") {
                conduitAuth { userId ->
                    val request = getModel<UpdateProfileRequest>()
                    userService.updateProfile(userId, request)
                }
            }

            // Admin: get any user by ID
            withRole(UserRole.Admin) {
                get("/{id}") {
                    conduitAuth { _ ->
                        val targetId = getStringParam("id")
                        userService.getUserById(targetId)
                    }
                }
            }
        }
    }
}
