package com.m2f.template

import arrow.continuations.SuspendApp
import arrow.fx.coroutines.resourceScope
import com.m2f.core.config.configuration.Configuration
import com.m2f.core.database.startDatabase
import com.m2f.template.startup.config
import com.m2f.template.startup.startServer
import io.ktor.server.application.Application
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.openapi.OpenAPISource
import io.ktor.server.plugins.openapi.openAPI
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.awaitCancellation
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase

fun main() = SuspendApp {
    resourceScope {
        config {
            val database = startDatabase()
            startServer(Netty) {
                context(database) {
                    module()
                }
            }
            awaitCancellation()
        }
    }
}

context(_: Configuration, _: R2dbcDatabase)
fun Application.module() {
    routing {
        openAPI("openapi")
    }
    routing {
        get("/amazing") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }
        openAPI("/docs") {
            source = OpenAPISource("openapi/generated-api.json")
        }
    }
}
