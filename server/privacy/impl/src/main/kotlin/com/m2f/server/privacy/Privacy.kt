package com.m2f.server.privacy

import com.m2f.core.database.migrations.Migration
import com.m2f.core.database.migrations.MigrationRegistry
import com.m2f.server.privacy.contract.tables.AccountDeletionRequestsTable
import com.m2f.server.privacy.contract.tables.ConsentRecordsTable
import com.m2f.server.privacy.contract.tables.DataExportRequestsTable
import com.m2f.server.privacy.contract.tables.LegalDocumentsTable
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils

internal class CreateConsentRecordsTableMigration : Migration {
    override val version: String = "20260312000010"
    override val description: String = "Create consent_records table"

    override suspend fun migrate() {
        SchemaUtils.create(ConsentRecordsTable)
    }
}

internal class CreateLegalDocumentsTableMigration : Migration {
    override val version: String = "20260312000011"
    override val description: String = "Create legal_documents table"

    override suspend fun migrate() {
        SchemaUtils.create(LegalDocumentsTable)
    }
}

internal class CreateDataExportRequestsTableMigration : Migration {
    override val version: String = "20260312000012"
    override val description: String = "Create data_export_requests table"

    override suspend fun migrate() {
        SchemaUtils.create(DataExportRequestsTable)
    }
}

internal class CreateAccountDeletionRequestsTableMigration : Migration {
    override val version: String = "20260312000013"
    override val description: String = "Create account_deletion_requests table"

    override suspend fun migrate() {
        SchemaUtils.create(AccountDeletionRequestsTable)
    }
}

fun registerPrivacyMigrations() {
    MigrationRegistry.register(CreateConsentRecordsTableMigration())
    MigrationRegistry.register(CreateLegalDocumentsTableMigration())
    MigrationRegistry.register(CreateDataExportRequestsTableMigration())
    MigrationRegistry.register(CreateAccountDeletionRequestsTableMigration())
}
