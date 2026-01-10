package dev.olek.lmclient.data.database

import app.cash.sqldelight.db.SqlDriver
import dev.olek.lmclient.shared.data.Database
import org.koin.core.annotation.Single

expect class DriverFactory() {
    fun createDriver(name: String): SqlDriver
}

@Single
internal fun createDatabase(driverFactory: DriverFactory = DriverFactory()): Database {
    val driver = driverFactory.createDriver("lmclient.db")
    return Database(driver)
}
