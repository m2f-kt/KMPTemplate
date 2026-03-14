package com.m2f.server.privacy.routes

import com.m2f.core.config.server.conduitAuth
import com.m2f.server.privacy.contract.service.ProcessingRestrictionService
import com.m2f.template.models.routes.Privacy
import io.ktor.server.auth.authenticate
import io.ktor.server.resources.post
import io.ktor.server.routing.Route

fun Route.restrictionRoutes(processingRestrictionService: ProcessingRestrictionService) {
    authenticate {
        post<Privacy.RestrictProcessing> {
            conduitAuth { userId ->
                processingRestrictionService.restrictProcessing(userId)
                mapOf("message" to "Processing restricted")
            }
        }
        post<Privacy.LiftRestriction> {
            conduitAuth { userId ->
                processingRestrictionService.liftRestriction(userId)
                mapOf("message" to "Restriction lifted")
            }
        }
    }
}
