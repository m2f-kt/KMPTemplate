package com.m2f.template.di

import arrow.core.raise.Raise
import com.m2f.core.config.server.DomainError
import com.m2f.server.ai.wire.aiWireModule
import com.m2f.server.privacy.wire.privacyWireModule
import com.m2f.server.auth.wire.authWireModule
import com.m2f.server.auth.wire.registerAuthService
import com.m2f.server.files.wire.fileWireModule
import com.m2f.server.groups.wire.groupWireModule
import com.m2f.server.groups.contract.service.InvitationService
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
    includes(authWireModule)
    includes(groupWireModule)
    includes(fileWireModule)
    includes(aiWireModule)
    includes(privacyWireModule)

    // AuthService is registered here (not in authWireModule) so we can wire
    // the invitation acceptance callback that bridges auth and groups modules.
    registerAuthService { raise: Raise<DomainError>, userId: String, request: RegisterRequest ->
        val token = request.invitationToken
        if (token != null) {
            val invitationService = org.koin.java.KoinJavaComponent.getKoin().get<InvitationService>()
            with(raise) {
                invitationService.acceptInvitation(
                    AcceptInvitationRequest(token),
                    userId,
                )
            }
        }
    }
}
