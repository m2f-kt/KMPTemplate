@file:Suppress("MatchingDeclarationName")

package com.m2f.server.auth.authorization

import com.m2f.template.models.UserRole
import com.m2f.template.models.dto.ErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.AuthenticationChecked
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.application.RouteScopedPlugin
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.routing.Route
import io.ktor.server.routing.RouteSelector
import io.ktor.server.routing.RouteSelectorEvaluation
import io.ktor.server.routing.RoutingResolveContext

/**
 * Configuration for the role-based authorization plugin.
 */
class RoleConfig {
    var roles: Set<UserRole> = emptySet()
}

/**
 * Route-scoped plugin that enforces role-based access control.
 * Reads the JWT "role" claim string and converts it to [UserRole] for type-safe comparison.
 * Must be used inside an `authenticate` block.
 */
val RoleAuthorizationPlugin: RouteScopedPlugin<RoleConfig> = createRouteScopedPlugin(
    name = "RoleAuthorizationPlugin",
    createConfiguration = ::RoleConfig,
) {
    val requiredRoles = pluginConfig.roles
    on(AuthenticationChecked) { call ->
        val principal = call.principal<JWTPrincipal>()
        val roleString = principal?.payload?.getClaim("role")?.asString()
        val userRole = roleString?.let { UserRole.fromString(it) }
        if (userRole == null || userRole !in requiredRoles) {
            call.respond(
                HttpStatusCode.Forbidden,
                ErrorResponse(code = "USER_FORBIDDEN", message = "Insufficient permissions"),
            )
        }
    }
}

/**
 * Convenience extension to install role-based authorization on a route.
 * Usage: `withRole(UserRole.Admin) { get("/admin-endpoint") { ... } }`
 */
fun Route.withRole(vararg roles: UserRole, build: Route.() -> Unit) {
    val authorizedRoute = createChild(object : RouteSelector() {
        override suspend fun evaluate(context: RoutingResolveContext, segmentIndex: Int) =
            RouteSelectorEvaluation.Transparent
        override fun toString() = "(roles: ${roles.joinToString { it.value }})"
    })
    authorizedRoute.install(RoleAuthorizationPlugin) { this.roles = roles.toSet() }
    authorizedRoute.build()
}
