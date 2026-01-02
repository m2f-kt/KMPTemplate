package com.m2f.core.database

import arrow.fx.coroutines.ResourceScope
import com.m2f.core.config.configuration.Configuration
import com.m2f.core.database.connection.DataSource
import com.m2f.core.database.migrations.MigrationRegistry
import com.m2f.core.database.migrations.Migrations
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.IsolationLevel
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase

context(_: ResourceScope, config: Configuration) suspend fun startDatabase(): R2dbcDatabase {
    val dataSource = DataSource()
    return R2dbcDatabase.connect {
        defaultMaxAttempts = config.maxDatabaseAttempts
        defaultR2dbcIsolationLevel = IsolationLevel.READ_COMMITTED

        connectionFactoryOptions {
            option(ConnectionFactoryOptions.DRIVER, dataSource.driver)
            option(ConnectionFactoryOptions.HOST, dataSource.host)
            option(ConnectionFactoryOptions.PORT, dataSource.port)
            option(ConnectionFactoryOptions.DATABASE, dataSource.database)
            option(ConnectionFactoryOptions.USER, dataSource.username)
            option(ConnectionFactoryOptions.PASSWORD, dataSource.password)
        }
    }.also {
        MigrationRegistry.registerMigrations()
        Migrations.migrate(it)
    }

}
