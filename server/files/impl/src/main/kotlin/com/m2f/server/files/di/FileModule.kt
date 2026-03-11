package com.m2f.server.files.di

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.smithy.kotlin.runtime.net.url.Url
import com.m2f.core.config.configuration.Configuration
import com.m2f.server.files.contract.service.FileService
import com.m2f.server.files.service.S3FileService
import org.koin.dsl.module

/**
 * Koin module wiring all file upload dependencies.
 */
val fileModule = module {
    single {
        val config: Configuration = get()
        S3Client {
            region = config.env.s3.region
            endpointUrl = Url.parse(config.env.s3.endpoint)
            credentialsProvider = StaticCredentialsProvider {
                accessKeyId = config.env.s3.accessKey
                secretAccessKey = config.env.s3.secretKey
            }
            forcePathStyle = true // Required for MinIO
        }
    }
    single<FileService> { S3FileService(get(), get()) }
}
