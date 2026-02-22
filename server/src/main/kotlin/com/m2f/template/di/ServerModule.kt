package com.m2f.template.di

import com.m2f.server.ai.di.aiModule
import com.m2f.server.auth.di.authModule
import com.m2f.server.files.di.fileModule
import com.m2f.server.groups.di.groupModule
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

/**
 * Server DI module aggregating all server feature modules.
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
    includes(groupModule)
    includes(fileModule)
    includes(aiModule)
}
