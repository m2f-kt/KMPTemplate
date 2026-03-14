package com.m2f.server.privacy.routes

import com.m2f.core.config.server.conduitAuth
import com.m2f.server.privacy.contract.service.DataExportService
import com.m2f.template.models.routes.Privacy
import io.ktor.server.auth.authenticate
import io.ktor.server.resources.get
import io.ktor.server.resources.post
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
    }
}
