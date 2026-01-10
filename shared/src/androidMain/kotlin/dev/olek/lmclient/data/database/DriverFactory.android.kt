package dev.olek.lmclient.data.database

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import dev.olek.lmclient.shared.data.Database
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

actual class DriverFactory : KoinComponent {
    private val context: Context by inject()

    actual fun createDriver(name: String): SqlDriver {
        val schema = Database.Schema.synchronous()
        return AndroidSqliteDriver(
            schema = schema,
            context = context,
            name = name,
            callback = object : AndroidSqliteDriver.Callback(schema) {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    db.setForeignKeyConstraintsEnabled(true)
                }
            },
        )
    }
}
