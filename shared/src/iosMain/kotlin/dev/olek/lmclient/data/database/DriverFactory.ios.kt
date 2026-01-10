package dev.olek.lmclient.data.database

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import co.touchlab.sqliter.DatabaseConfiguration
import dev.olek.lmclient.shared.data.Database

actual class DriverFactory {
    actual fun createDriver(name: String): SqlDriver {
        val schema = Database.Schema.synchronous()
        return NativeSqliteDriver(schema, name, onConfiguration = { config ->
            config.copy(
                extendedConfig = DatabaseConfiguration.Extended(
                    foreignKeyConstraints = true,
                ),
            )
        })
    }
}
