package com.m2f.server.groups.di

import com.m2f.core.config.configuration.Configuration
import com.m2f.server.groups.contract.repository.MembershipRepository
import com.m2f.server.groups.contract.service.GroupService
import com.m2f.server.groups.contract.service.InvitationService
import com.m2f.server.groups.repository.GroupRepository
import com.m2f.server.groups.repository.InvitationRepository
import com.m2f.server.groups.repository.MembershipRepositoryImpl
import com.m2f.server.groups.service.GroupServiceImpl
import com.m2f.server.groups.service.InvitationServiceImpl
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.koin.dsl.module

/**
 * Koin module wiring all group dependencies.
 */
val groupModule = module {
    single { GroupRepository(get<R2dbcDatabase>()) }
    single<MembershipRepository> { MembershipRepositoryImpl(get<R2dbcDatabase>()) }
    single { InvitationRepository(get<R2dbcDatabase>()) }
    single<GroupService> { GroupServiceImpl(get(), get(), get(), get()) }
    single<InvitationService> { InvitationServiceImpl(get(), get(), get(), get(), get(), get<Configuration>()) }
}
