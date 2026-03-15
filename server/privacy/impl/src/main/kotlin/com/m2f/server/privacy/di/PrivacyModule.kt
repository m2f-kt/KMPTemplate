package com.m2f.server.privacy.di

import com.m2f.server.privacy.contract.repository.AccountDeletionRepository
import com.m2f.server.privacy.contract.repository.ConsentRepository
import com.m2f.server.privacy.contract.repository.DataExportRepository
import com.m2f.server.privacy.contract.repository.LegalDocumentRepository
import com.m2f.server.privacy.contract.service.AccountDeletionService
import com.m2f.server.privacy.contract.service.ConsentService
import com.m2f.server.privacy.contract.service.DataExportService
import com.m2f.server.privacy.contract.service.LegalDocumentService
import com.m2f.server.privacy.jobs.DeletionExecutorJob
import com.m2f.server.privacy.jobs.ExportCleanupJob
import com.m2f.server.privacy.jobs.ExportProcessorJob
import com.m2f.server.privacy.jobs.PrivacyJob
import com.m2f.server.privacy.jobs.PrivacyJobScheduler
import com.m2f.server.privacy.repository.ExposedAccountDeletionRepository
import com.m2f.server.privacy.repository.ExposedConsentRepository
import com.m2f.server.privacy.repository.ExposedDataExportRepository
import com.m2f.server.privacy.repository.ExposedLegalDocumentRepository
import com.m2f.server.privacy.service.AccountDeletionServiceImpl
import com.m2f.server.privacy.service.ConsentServiceImpl
import com.m2f.server.privacy.service.DataExportServiceImpl
import com.m2f.server.privacy.service.LegalDocumentServiceImpl
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.koin.dsl.module

val privacyModule = module {
    single<ConsentRepository> { ExposedConsentRepository(get<R2dbcDatabase>()) }
    single<LegalDocumentRepository> { ExposedLegalDocumentRepository(get<R2dbcDatabase>()) }
    single<DataExportRepository> { ExposedDataExportRepository(get<R2dbcDatabase>()) }
    single<AccountDeletionRepository> { ExposedAccountDeletionRepository(get<R2dbcDatabase>()) }
    single<ConsentService> { ConsentServiceImpl(get(), get()) }
    single<LegalDocumentService> { LegalDocumentServiceImpl(get()) }
    single<DataExportService> { DataExportServiceImpl(get(), getAll()) }
    single<AccountDeletionService> { AccountDeletionServiceImpl(get(), get(), get(), get()) }
    single<PrivacyJob>(qualifier = org.koin.core.qualifier.named("deletionExecutorJob")) {
        DeletionExecutorJob(get(), get(), get())
    }
    single<PrivacyJob>(qualifier = org.koin.core.qualifier.named("exportCleanupJob")) {
        ExportCleanupJob(get())
    }
    single<PrivacyJob>(qualifier = org.koin.core.qualifier.named("exportProcessorJob")) {
        ExportProcessorJob(get(), getAll())
    }
    single { PrivacyJobScheduler(get<CoroutineScope>(), getAll()) }
}
