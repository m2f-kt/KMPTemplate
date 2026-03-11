package com.m2f.server.groups.routes

import com.m2f.core.config.server.conduit
import com.m2f.core.config.server.conduitAuth
import com.m2f.core.config.server.getModel
import com.m2f.server.auth.contract.authorization.withRole
import com.m2f.server.groups.contract.service.GroupService
import com.m2f.template.models.UserRole
import com.m2f.template.models.dto.AddMemberRequest
import com.m2f.template.models.dto.CreateGroupRequest
import com.m2f.template.models.dto.RegisterMemberRequest
import com.m2f.template.models.dto.UpdateGroupRequest
import com.m2f.template.models.routes.Groups
import com.m2f.template.models.routes.Users
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext

/**
 * Group management routes.
 * All routes require JWT authentication.
 * System-level authorization (Admin+, PowerAdmin) enforced via withRole plugin.
 * Group-level authorization (OWNER, ADMIN, MEMBER) enforced in GroupService.
 */
fun Route.groupRoutes(groupService: GroupService) {
    authenticate {
        // Get current user's group memberships -- no role restriction, any authenticated user
        get<Users.Me.Memberships> {
            conduitAuth { userId ->
                groupService.getMyMemberships(userId)
            }
        }

        // Create group -- Admin+ system role required
        withRole(UserRole.Admin, UserRole.PowerAdmin) {
            post<Groups.Create> {
                conduitAuth(HttpStatusCode.Created) { userId ->
                    val request = getModel<CreateGroupRequest>()
                    groupService.createGroup(request, userId)
                }
            }
        }

        // Get group by ID -- group-level auth checked in service
        get<Groups.ById> { route ->
            conduitAuth { userId ->
                val role = getUserRole()
                groupService.getGroup(route.groupId, userId, role)
            }
        }

        // Update group -- group-level auth checked in service
        post<Groups.Update> { route ->
            conduitAuth { userId ->
                val request = getModel<UpdateGroupRequest>()
                val role = getUserRole()
                groupService.updateGroup(route.groupId, request, userId, role)
            }
        }

        // Delete group -- group-level auth checked in service
        post<Groups.Delete> { route ->
            conduitAuth { userId ->
                val role = getUserRole()
                groupService.deleteGroup(route.groupId, userId, role)
                mapOf("message" to "Group deleted")
            }
        }

        // List all groups -- PowerAdmin only
        withRole(UserRole.PowerAdmin) {
            get<Groups.ListAll> {
                conduit {
                    groupService.listAllGroups()
                }
            }
        }

        // List members of a group -- group-level auth checked in service
        get<Groups.Members> { route ->
            conduitAuth { userId ->
                val role = getUserRole()
                groupService.getMembers(route.groupId, userId, role, route.cursor, route.limit)
            }
        }

        // Add existing user to group -- group-level auth checked in service
        post<Groups.AddMember> { route ->
            conduitAuth { userId ->
                val request = getModel<AddMemberRequest>()
                val role = getUserRole()
                groupService.addMember(route.groupId, request, userId, role)
            }
        }

        // Remove member from group -- group-level auth checked in service
        post<Groups.RemoveMember> { route ->
            conduitAuth { userId ->
                val role = getUserRole()
                groupService.removeMember(route.groupId, route.userId, userId, role)
                mapOf("message" to "Member removed")
            }
        }

        // Register new user and add to group -- group-level auth checked in service
        post<Groups.RegisterMember> { route ->
            conduitAuth { userId ->
                val request = getModel<RegisterMemberRequest>()
                val role = getUserRole()
                groupService.registerMember(route.groupId, request, userId, role)
            }
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
