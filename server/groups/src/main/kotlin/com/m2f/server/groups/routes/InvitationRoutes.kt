package com.m2f.server.groups.routes

import com.m2f.core.config.server.conduit
import com.m2f.core.config.server.conduitAuth
import com.m2f.core.config.server.getModel
import com.m2f.server.auth.authorization.withRole
import com.m2f.server.groups.service.InvitationService
import com.m2f.template.models.UserRole
import com.m2f.template.models.dto.AcceptInvitationRequest
import com.m2f.template.models.dto.CreateInvitationRequest
import com.m2f.template.models.routes.Groups
import com.m2f.template.models.routes.Invitations
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext

/**
 * Invitation management routes.
 *
 * Create invitation: POST /api/groups/{groupId}/invitations/create (requires auth + admin)
 * Get invitation: GET /api/invitations/{token} (public, no auth)
 * Accept invitation: POST /api/invitations/accept (requires auth)
 */
fun Route.invitationRoutes(invitationService: InvitationService) {
    // Create invitation -- requires auth + ADMIN/OWNER or PowerAdmin
    authenticate {
        withRole(UserRole.Admin, UserRole.PowerAdmin) {
            post<Groups.CreateInvitation> { route ->
                conduitAuth(HttpStatusCode.Created) { userId ->
                    val request = getModel<CreateInvitationRequest>()
                    val role = getUserRole()
                    invitationService.createInvitation(route.groupId, request, userId, role)
                }
            }
        }

        // Accept invitation -- requires auth (any role)
        post<Invitations.Accept> {
            conduitAuth { userId ->
                val request = getModel<AcceptInvitationRequest>()
                invitationService.acceptInvitation(request, userId)
            }
        }
    }

    // Get invitation by token -- public, no auth required
    get<Invitations.ByToken> { route ->
        conduit {
            invitationService.getInvitation(route.token)
        }
    }
}

/**
 * Extract the [UserRole] from the JWT principal's "role" claim.
 * Defaults to [UserRole.User] if the claim is missing or unrecognized.
 */
private fun RoutingContext.getUserRole(): UserRole {
    val roleString = call.principal<JWTPrincipal>()?.payload?.getClaim("role")?.asString()
    return roleString?.let { UserRole.fromString(it) } ?: UserRole.User
}
