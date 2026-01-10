package dev.olek.lmclient.data.database

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.olek.lmclient.shared.data.Database
import java.util.Properties

actual class DriverFactory {
    actual fun createDriver(name: String): SqlDriver {
        val schema = Database.Schema.synchronous()

        val url = "jdbc:sqlite:$name"
        return JdbcSqliteDriver(url = url, properties = Properties(), schema = schema)
    }
}
