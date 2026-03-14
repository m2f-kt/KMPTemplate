package com.m2f.server.privacy.routes

import com.m2f.core.config.server.conduitAuth
import com.m2f.core.config.server.getModel
import com.m2f.server.privacy.contract.service.ConsentService
import com.m2f.template.models.dto.privacy.GrantConsentRequest
import com.m2f.template.models.routes.Privacy
import io.ktor.server.auth.authenticate
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.routing.Route

fun Route.consentRoutes(consentService: ConsentService) {
    authenticate {
        get<Privacy.GetConsents> {
            conduitAuth { userId ->
                consentService.getActiveConsents(userId)
            }
        }
        post<Privacy.GrantConsent> {
            val ipAddress = call.request.local.remoteAddress
            val userAgent = call.request.headers["User-Agent"]
            conduitAuth { userId ->
                val request = getModel<GrantConsentRequest>()
                consentService.grantConsent(userId, request, ipAddress, userAgent)
                mapOf("message" to "Consent granted")
            }
        }
        post<Privacy.WithdrawConsent> { route ->
            val ipAddress = call.request.local.remoteAddress
            val userAgent = call.request.headers["User-Agent"]
            conduitAuth { userId ->
                consentService.withdrawConsent(userId, route.type, ipAddress, userAgent)
                mapOf("message" to "Consent withdrawn")
            }
        }
        get<Privacy.RequiredConsents> {
            conduitAuth { userId ->
                consentService.getRequiredConsents(userId)
            }
        }
    }
}
