package com.m2f.template.di

import arrow.core.raise.Raise
import com.m2f.core.config.server.DomainError
import com.m2f.server.ai.di.aiModule
import com.m2f.server.auth.di.authModule
import com.m2f.server.auth.di.emailModule
import com.m2f.server.auth.service.AuthService
import com.m2f.server.files.di.fileModule
import com.m2f.server.groups.di.groupModule
import com.m2f.server.groups.service.InvitationService
import com.m2f.template.models.dto.AcceptInvitationRequest
import com.m2f.template.models.dto.RegisterRequest
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

/**
 * Server DI module aggregating all server feature modules.
 * Registers AuthService with invitation acceptance callback.
 */
val serverModule = module {
    single {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }
    includes(authModule)
    includes(emailModule)
    includes(groupModule)
    includes(fileModule)
    includes(aiModule)

    // AuthService is registered here (not in authModule) so we can wire
    // the invitation acceptance callback that bridges auth and groups modules.
    single {
        val invitationService = get<InvitationService>()
        AuthService(
            userRepository = get(),
            refreshTokenRepository = get(),
            passwordHasher = get(),
            tokenProvider = get(),
            onRegistered = { raise: Raise<DomainError>, userId: String, request: RegisterRequest ->
                val token = request.invitationToken
                if (token != null) {
                    with(raise) {
                        invitationService.acceptInvitation(
                            AcceptInvitationRequest(token),
                            userId,
                        )
                    }
                }
            },
        )
    }
}
