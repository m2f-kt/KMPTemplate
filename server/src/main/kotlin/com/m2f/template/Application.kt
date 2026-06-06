package com.m2f.template

import arrow.continuations.SuspendApp
import arrow.fx.coroutines.resourceScope
import com.m2f.core.config.configuration.Configuration
import com.m2f.core.config.configuration.configurationModule
import com.m2f.core.database.startDatabase
import com.m2f.core.security.configureSecurity
import com.m2f.server.ai.agents.AssistantAgentService
import com.m2f.server.ai.agents.ChatAgentService
import com.m2f.server.ai.rag.DocumentIngestionService
import com.m2f.server.ai.rag.DocumentRepository
import com.m2f.server.ai.wire.registerAiWireMigrations
import com.m2f.server.ai.routes.aiRoutes
import com.m2f.server.ai.routes.documentRoutes
import com.m2f.server.groups.contract.repository.MembershipRepository
import com.m2f.template.models.GroupRole
import com.m2f.core.database.migrations.registerVectorMigrations
import com.m2f.server.auth.wire.registerAuthWireMigrations
import com.m2f.server.privacy.wire.registerPrivacyWireMigrations
import com.m2f.server.auth.contract.repository.UserRepository
import com.m2f.server.auth.routes.authRoutes
import com.m2f.server.files.routes.fileRoutes
import com.m2f.server.files.contract.service.FileService
import com.m2f.server.groups.wire.registerGroupWireMigrations
import com.m2f.server.groups.routes.groupRoutes
import com.m2f.server.groups.routes.invitationRoutes
import com.m2f.server.groups.contract.service.GroupService
import com.m2f.server.groups.contract.service.InvitationService
import com.m2f.server.privacy.contract.service.AccountDeletionService
import com.m2f.server.privacy.contract.service.ConsentService
import com.m2f.server.privacy.contract.service.DataExportService
import com.m2f.server.privacy.contract.service.LegalDocumentService
import com.m2f.server.privacy.jobs.PrivacyJobScheduler
import com.m2f.server.privacy.routes.consentRoutes
import com.m2f.server.privacy.routes.deletionRoutes
import com.m2f.server.privacy.routes.exportRoutes
import com.m2f.server.privacy.routes.legalRoutes
import com.m2f.server.auth.routes.oauthRoutes
import com.m2f.server.auth.routes.userRoutes
import com.m2f.server.auth.contract.service.AuthService
import com.m2f.server.auth.service.OAuthService
import com.m2f.server.auth.service.PasswordResetService
import com.m2f.server.auth.service.UserService
import com.m2f.template.di.serverModule
import com.m2f.template.models.dto.ErrorResponse
import com.m2f.template.routes.avatarRoutes
import com.m2f.template.routes.healthRoutes
import com.m2f.template.startup.config
import com.m2f.template.startup.startServer
import io.ktor.client.HttpClient
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.OAuthServerSettings
import io.ktor.server.auth.authentication
import io.ktor.server.auth.oauth
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.resources.Resources
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import org.slf4j.LoggerFactory
import io.ktor.server.websocket.WebSockets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import org.koin.ktor.ext.getKoin
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin

fun main() = SuspendApp {
    resourceScope {
        config {
            registerAuthWireMigrations()
            registerGroupWireMigrations()
            registerAiWireMigrations()
            registerPrivacyWireMigrations()
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
    // Boot-time config validation: refuse to serve traffic when the JWT secret is
    // left at its committed placeholder in production, or a configured URL is malformed.
    // A Left aborts startup so orchestrators restart once the operator fixes the env.
    Configuration.validate(config.env).fold(
        ifLeft = { bootError -> error(bootError.message) },
        ifRight = { /* OK — proceed with plugin installs */ },
    )

    // Startup diagnostic: log config key LENGTHS + boolean flags only — NEVER raw secrets.
    LoggerFactory.getLogger("AppStartup").info(
        "Config loaded: jwtSecret.len={}, ai.enabled={}, googleApiKey.len={}, " +
            "s3.secretKey.len={}, smtp.password.len={}, baseUrl={}",
        config.env.auth.secret.length,
        config.env.ai.enabled,
        config.env.ai.googleApiKey.length,
        config.env.s3.secretKey.length,
        config.env.email.password.length,
        config.env.http.baseUrl,
    )

    install(Koin) {
        modules(configurationModule, serverModule)
    }
    getKoin().declare(database)
    getKoin().declare(CoroutineScope(coroutineContext))

    install(Resources)
    install(ContentNegotiation) { json() }
    install(WebSockets)
    installCors(config.env.http.port, config.env.http.corsAllowedOrigins)
    installStatusPages()
    configureSecurity()
    configureOAuth()

    val privacyJobScheduler: PrivacyJobScheduler by inject()
    privacyJobScheduler.start()

    installAppRouting()
}

private fun Application.installCors(serverPort: Int, extraOrigins: List<String>) {
    install(CORS) {
        // Development origins: API's own port + common WASM/dev-server defaults.
        allowHost("localhost:$serverPort", schemes = listOf("http"))
        allowHost("localhost:8080", schemes = listOf("http"))
        allowHost("localhost:8081", schemes = listOf("http"))
        allowHost("localhost:8082", schemes = listOf("http"))
        allowHost("localhost:3000", schemes = listOf("http"))
        // Extra origins from CORS_ALLOWED_ORIGINS env var (LAN IPs, ngrok, etc.)
        extraOrigins.forEach { origin ->
            allowHost(origin, schemes = listOf("http", "https"))
        }
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.AcceptLanguage)
        allowCredentials = true
    }
}

/** Global exception handler — prevents leaking internal details (SQL errors, stack traces). */
private fun Application.installStatusPages() {
    install(StatusPages) {
        val logger = LoggerFactory.getLogger("StatusPages")
        exception<Throwable> { call, cause ->
            logger.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(
                    code = "SERVER_INTERNAL_ERROR",
                    message = "An unexpected error occurred",
                ),
            )
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
context(config: Configuration, database: R2dbcDatabase)
private fun Application.installAppRouting() {
    val authService: AuthService by inject()
    val passwordResetService: PasswordResetService by inject()
    val userService: UserService by inject()
    val userRepository: UserRepository by inject()
    val oauthService: OAuthService by inject()
    val groupService: GroupService by inject()
    val invitationService: InvitationService by inject()
    val fileService: FileService by inject()
    val consentService: ConsentService by inject()
    val legalDocumentService: LegalDocumentService by inject()
    val dataExportService: DataExportService by inject()
    val accountDeletionService: AccountDeletionService by inject()
    val membershipRepository: MembershipRepository by inject()
    val assistantAgentService: AssistantAgentService by inject()
    val chatAgentService: ChatAgentService by inject()
    val documentIngestionService: DocumentIngestionService by inject()
    val documentRepository: DocumentRepository by inject()

    val roleChecker: suspend (String, String) -> Boolean = { userId, groupId ->
        val membership = membershipRepository.findByUserAndGroup(
            Uuid.parse(userId),
            Uuid.parse(groupId),
        )
        membership != null && GroupRole.fromString(membership.role).level >= GroupRole.Admin.level
    }

    routing {
        healthRoutes(database, config.env)
        authRoutes(authService, passwordResetService)
        oauthRoutes(oauthService, config.env.oauth)
        userRoutes(userService)
        groupRoutes(groupService)
        invitationRoutes(invitationService)
        fileRoutes(fileService)
        avatarRoutes(userRepository, fileService)
        consentRoutes(consentService)
        legalRoutes(legalDocumentService)
        exportRoutes(dataExportService)
        deletionRoutes(accountDeletionService)
        aiRoutes(assistantAgentService, chatAgentService, roleChecker = roleChecker)
        documentRoutes(
            documentIngestionService = documentIngestionService,
            documentRepository = documentRepository,
            fileUploader = { userId, fileName, contentType, bytes ->
                fileService.upload(userId, fileName, contentType, bytes).key
            },
            fileDeleter = { /* S3 cleanup is best-effort; FileService has no delete yet */ },
            roleChecker = roleChecker,
            aiDispatcher = config.aiDispatcher,
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
