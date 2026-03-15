package com.m2f.server.privacy.routes

import arrow.core.raise.either
import com.m2f.core.config.server.DomainError
import com.m2f.core.config.server.conduitAuth
import com.m2f.server.privacy.contract.service.DataExportService
import com.m2f.template.models.routes.Privacy
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route

fun Route.exportRoutes(dataExportService: DataExportService) {
    authenticate {
        post<Privacy.RequestExport> {
            conduitAuth { userId ->
                dataExportService.requestExport(userId)
            }
        }
        get<Privacy.ExportStatus> { route ->
            conduitAuth { userId ->
                dataExportService.getExportStatus(userId, route.id)
            }
        }
        get<Privacy.ExportDownload> { route ->
            conduitAuth { userId ->
                mapOf("downloadUrl" to dataExportService.getExportDownloadUrl(userId, route.id))
            }
        }
        get<Privacy.ActiveExport> {
            val principal = call.principal<JWTPrincipal>()
            if (principal == null) {
                call.respond(HttpStatusCode.Unauthorized)
                return@get
            }
            val userId = principal.payload.subject
            val result = either<DomainError, _> {
                dataExportService.getActiveExport(userId)
            }
            result.fold(
                { call.respond(HttpStatusCode.InternalServerError) },
                { export ->
                    if (export == null) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        call.respond(HttpStatusCode.OK, export)
                    }
                },
            )
        }
    }
}
