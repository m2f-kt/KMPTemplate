package com.m2f.template

import arrow.continuations.SuspendApp
import arrow.fx.coroutines.resourceScope
import com.m2f.core.config.configuration.Configuration
import com.m2f.core.database.startDatabase
import com.m2f.core.security.configureSecurity
import com.m2f.server.auth.registerAuthMigrations
import com.m2f.server.auth.routes.authRoutes
import com.m2f.server.auth.routes.userRoutes
import com.m2f.server.auth.service.AuthService
import com.m2f.server.auth.service.UserService
import com.m2f.template.startup.config
import com.m2f.template.startup.startServer
import com.m2f.core.config.configuration.configurationModule
import com.m2f.template.di.serverModule
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.awaitCancellation
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.koin.ktor.ext.getKoin
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin

fun main() = SuspendApp {
    resourceScope {
        config {
            registerAuthMigrations()
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

context(_: Configuration, database: R2dbcDatabase)
fun Application.module() {
    install(Koin) {
        modules(configurationModule, serverModule)
    }
    // Register the R2dbcDatabase instance in Koin for repository injection
    getKoin().declare(database)

    install(ContentNegotiation) { json() }
    configureSecurity()
    routing {
        get("/amazing") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }
        val authService: AuthService by inject()
        val userService: UserService by inject()
        authRoutes(authService)
        userRoutes(userService)
    }
}
