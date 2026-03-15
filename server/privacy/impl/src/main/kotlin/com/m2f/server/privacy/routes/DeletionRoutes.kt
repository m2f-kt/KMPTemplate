package com.m2f.server.privacy.routes

import arrow.core.raise.either
import com.m2f.core.config.server.DomainError
import com.m2f.core.config.server.conduitAuth
import com.m2f.core.config.server.getModel
import com.m2f.server.privacy.contract.service.AccountDeletionService
import com.m2f.template.models.dto.privacy.DeletionRequest
import com.m2f.template.models.routes.Privacy
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route

fun Route.deletionRoutes(accountDeletionService: AccountDeletionService) {
    authenticate {
        post<Privacy.RequestDeletion> {
            conduitAuth { userId ->
                val request = getModel<DeletionRequest>()
                accountDeletionService.requestDeletion(userId, request)
            }
        }
        get<Privacy.GetDeletionStatus> {
            val principal = call.principal<JWTPrincipal>()
            if (principal == null) {
                call.respond(HttpStatusCode.Unauthorized)
                return@get
            }
            val userId = principal.payload.subject
            val result = either<DomainError, _> {
                accountDeletionService.getDeletionStatus(userId)
            }
            result.fold(
                { call.respond(HttpStatusCode.InternalServerError) },
                { status ->
                    if (status == null) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        call.respond(HttpStatusCode.OK, status)
                    }
                },
            )
        }
        post<Privacy.CancelDeletion> {
            conduitAuth { userId ->
                accountDeletionService.cancelDeletion(userId)
                mapOf("message" to "Deletion cancelled")
            }
        }
    }
}
