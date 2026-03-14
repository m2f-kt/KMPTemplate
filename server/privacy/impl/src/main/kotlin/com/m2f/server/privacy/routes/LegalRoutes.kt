package com.m2f.server.privacy.routes

import com.m2f.core.config.server.conduit
import com.m2f.server.privacy.contract.service.LegalDocumentService
import com.m2f.template.models.routes.Privacy
import io.ktor.server.resources.get
import io.ktor.server.routing.Route

fun Route.legalRoutes(legalDocumentService: LegalDocumentService) {
    get<Privacy.LegalDocument> { route ->
        conduit {
            legalDocumentService.getCurrentDocument(route.type, route.locale)
        }
    }
    get<Privacy.LegalDocumentVersions> { route ->
        conduit {
            legalDocumentService.getAllVersions(route.type)
        }
    }
}
