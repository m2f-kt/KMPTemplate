package com.m2f.template

import arrow.continuations.SuspendApp
import arrow.fx.coroutines.resourceScope
import com.m2f.core.config.configuration.Configuration
import com.m2f.core.config.configuration.configurationModule
import com.m2f.core.database.startDatabase
import com.m2f.core.security.configureSecurity
import com.m2f.server.ai.agents.AssistantAgentService
import com.m2f.server.ai.agents.ChatAgentService
import com.m2f.server.ai.registerAiMigrations
import com.m2f.server.ai.routes.aiRoutes
import com.m2f.core.database.migrations.registerVectorMigrations
import com.m2f.server.auth.registerAuthMigrations
import com.m2f.server.auth.repository.UserRepository
import com.m2f.server.auth.routes.authRoutes
import com.m2f.server.files.routes.fileRoutes
import com.m2f.server.files.service.FileService
import com.m2f.server.groups.registerGroupMigrations
import com.m2f.server.groups.routes.groupRoutes
import com.m2f.server.groups.routes.invitationRoutes
import com.m2f.server.groups.service.GroupService
import com.m2f.server.groups.service.InvitationService
import com.m2f.server.auth.routes.oauthRoutes
import com.m2f.server.auth.routes.userRoutes
import com.m2f.server.auth.service.AuthService
import com.m2f.server.auth.service.OAuthService
import com.m2f.server.auth.service.PasswordResetService
import com.m2f.server.auth.service.UserService
import com.m2f.template.di.serverModule
import com.m2f.template.routes.avatarRoutes
import com.m2f.template.startup.config
import com.m2f.template.startup.startServer
import io.ktor.client.HttpClient
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.OAuthServerSettings
import io.ktor.server.auth.authentication
import io.ktor.server.auth.oauth
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.resources.Resources
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import kotlinx.coroutines.awaitCancellation
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.koin.ktor.ext.getKoin
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin

fun main() = SuspendApp {
    resourceScope {
        config {
            registerAuthMigrations()
            registerGroupMigrations()
            registerAiMigrations()
            registerVectorMigrations()
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

context(config: Configuration, database: R2dbcDatabase)
fun Application.module() {
    install(Koin) {
        modules(configurationModule, serverModule)
    }
    // Register the R2dbcDatabase instance in Koin for repository injection
    getKoin().declare(database)

    install(Resources)
    install(ContentNegotiation) { json() }
    install(WebSockets)
    install(CORS) {
        // Development origins (WASM dev server + backend)
        allowHost("localhost:8080", schemes = listOf("http"))
        allowHost("localhost:8081", schemes = listOf("http"))
        allowHost("localhost:8082", schemes = listOf("http"))
        allowHost("localhost:3000", schemes = listOf("http"))
        // HTTP methods used by API endpoints
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)
        // Headers used by API (Content-Type for JSON, Authorization for Bearer tokens)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.AcceptLanguage)
        // Allow credentials (needed for auth flows)
        allowCredentials = true
    }
    configureSecurity()
    configureOAuth()
    routing {
        val authService: AuthService by inject()
        val passwordResetService: PasswordResetService by inject()
        val userService: UserService by inject()
        val userRepository: UserRepository by inject()
        val oauthService: OAuthService by inject()
        authRoutes(authService, passwordResetService)
        oauthRoutes(oauthService, config.env.oauth)
        userRoutes(userService)
        val groupService: GroupService by inject()
        groupRoutes(groupService)
        val invitationService: InvitationService by inject()
        invitationRoutes(invitationService)
        val fileService: FileService by inject()
        fileRoutes(fileService)
        avatarRoutes(userRepository, fileService)
        val assistantAgentService: AssistantAgentService by inject()
        val chatAgentService: ChatAgentService by inject()
        aiRoutes(
            assistantAgentService,
            chatAgentService,
        )
    }
}

/**
 * Install OAuth authentication providers for Google and Apple.
 * Non-functional until environment variables are configured.
 */
context(config: Configuration)
private fun Application.configureOAuth() {
    val httpClient: HttpClient by inject()
    val baseUrl = "http://${config.env.http.host}:${config.env.http.port}"

    authentication {
        oauth("google-oauth") {
            urlProvider = { "$baseUrl/api/auth/oauth/google/callback" }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "google",
                    authorizeUrl = "https://accounts.google.com/o/oauth2/v2/auth",
                    accessTokenUrl = "https://oauth2.googleapis.com/token",
                    clientId = config.env.oauth.googleClientId,
                    clientSecret = config.env.oauth.googleClientSecret,
                    requestMethod = HttpMethod.Post,
                    defaultScopes = listOf("openid", "profile", "email"),
                )
            }
            client = httpClient
        }
        oauth("apple-oauth") {
            urlProvider = { "$baseUrl/api/auth/oauth/apple/callback" }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "apple",
                    authorizeUrl = "https://appleid.apple.com/auth/authorize",
                    accessTokenUrl = "https://appleid.apple.com/auth/token",
                    clientId = config.env.oauth.appleClientId,
                    clientSecret = config.env.oauth.appleClientSecret,
                    requestMethod = HttpMethod.Post,
                    defaultScopes = listOf("name", "email"),
                )
            }
            client = httpClient
        }
    }
}
