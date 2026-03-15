package com.m2f.server.privacy

import com.m2f.core.database.migrations.Migration
import com.m2f.core.database.migrations.MigrationRegistry
import com.m2f.server.privacy.contract.tables.AccountDeletionRequestsTable
import com.m2f.server.privacy.contract.tables.ConsentRecordsTable
import com.m2f.server.privacy.contract.tables.DataExportRequestsTable
import com.m2f.server.privacy.contract.tables.LegalDocumentsTable
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.insert

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

internal class SeedMarketingAnalyticsDocumentsMigration : Migration {
    override val version: String = "20260315000002"
    override val description: String = "Seed legal documents for Marketing and Analytics consent types"

    override suspend fun migrate() {
        LegalDocumentsTable.insert {
            it[type] = "MARKETING"
            it[version] = "1.0.0"
            it[locale] = "en"
            it[content] = "By enabling marketing communications, you consent to receive promotional emails, " +
                "newsletters, and product updates. You can withdraw this consent at any time through your " +
                "privacy settings."
        }
        LegalDocumentsTable.insert {
            it[type] = "MARKETING"
            it[version] = "1.0.0"
            it[locale] = "es"
            it[content] = "Al activar las comunicaciones de marketing, usted consiente recibir correos " +
                "promocionales, boletines informativos y actualizaciones de productos. Puede retirar este " +
                "consentimiento en cualquier momento a través de su configuración de privacidad."
        }
        LegalDocumentsTable.insert {
            it[type] = "ANALYTICS"
            it[version] = "1.0.0"
            it[locale] = "en"
            it[content] = "By enabling analytics, you consent to the collection of usage data including page " +
                "views, feature interactions, and performance metrics. This data is used to improve the " +
                "application experience. You can withdraw this consent at any time through your privacy settings."
        }
        LegalDocumentsTable.insert {
            it[type] = "ANALYTICS"
            it[version] = "1.0.0"
            it[locale] = "es"
            it[content] = "Al activar los análisis, usted consiente la recopilación de datos de uso, " +
                "incluyendo páginas visitadas, interacciones con funcionalidades y métricas de rendimiento. " +
                "Estos datos se utilizan para mejorar la experiencia de la aplicación. Puede retirar este " +
                "consentimiento en cualquier momento a través de su configuración de privacidad."
        }
    }
}

fun registerPrivacyMigrations() {
    MigrationRegistry.register(CreateConsentRecordsTableMigration())
    MigrationRegistry.register(CreateLegalDocumentsTableMigration())
    MigrationRegistry.register(CreateDataExportRequestsTableMigration())
    MigrationRegistry.register(CreateAccountDeletionRequestsTableMigration())
    MigrationRegistry.register(SeedMarketingAnalyticsDocumentsMigration())
}
