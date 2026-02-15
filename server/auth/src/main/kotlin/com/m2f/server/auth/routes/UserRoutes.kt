package com.m2f.server.auth.routes

import com.m2f.core.config.server.conduitAuth
import com.m2f.core.config.server.getModel
import com.m2f.server.auth.authorization.withRole
import com.m2f.server.auth.service.UserService
import com.m2f.template.models.UserRole
import com.m2f.template.models.dto.UpdateProfileRequest
import com.m2f.template.models.routes.Users
import io.ktor.server.auth.authenticate
import io.ktor.server.resources.get
import io.ktor.server.resources.put
import io.ktor.server.routing.Route

/**
 * User profile management routes.
 * All routes require JWT authentication.
 * Admin-only routes are additionally protected by the RBAC plugin.
 * Uses type-safe @Resource handlers via Ktor Resources.
 */
fun Route.userRoutes(userService: UserService) {
    authenticate {
        // Get own profile
        get<Users.Me> {
            conduitAuth { userId ->
                userService.getProfile(userId)
            }
        }

        // Update own profile
        put<Users.Me> {
            conduitAuth { userId ->
                val request = getModel<UpdateProfileRequest>()
                userService.updateProfile(userId, request)
            }
        }

        // Admin: get any user by ID
        withRole(UserRole.Admin) {
            get<Users.ById> { resource ->
                conduitAuth { _ ->
                    userService.getUserById(resource.id)
                }
            }
        }
    }
}
