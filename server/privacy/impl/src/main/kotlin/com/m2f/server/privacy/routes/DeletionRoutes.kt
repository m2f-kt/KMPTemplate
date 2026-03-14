package com.m2f.server.privacy.routes

import com.m2f.core.config.server.conduitAuth
import com.m2f.core.config.server.getModel
import com.m2f.server.privacy.contract.service.AccountDeletionService
import com.m2f.template.models.dto.privacy.DeletionRequest
import com.m2f.template.models.routes.Privacy
import io.ktor.server.auth.authenticate
import io.ktor.server.resources.get
import io.ktor.server.resources.post
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
            conduitAuth { userId ->
                accountDeletionService.getDeletionStatus(userId)
                    ?: mapOf("status" to "none")
            }
        }
        post<Privacy.CancelDeletion> {
            conduitAuth { userId ->
                accountDeletionService.cancelDeletion(userId)
                mapOf("message" to "Deletion cancelled")
            }
        }
    }
}
