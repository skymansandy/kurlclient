package dev.skymansandy.kurl.store

import app.cash.sqldelight.db.SqlDriver
import dev.skymansandy.kurlstore.db.KurlDatabase

object AppDatabase {

    private var _db: KurlDatabase? = null

    fun init(driver: SqlDriver) {
        if (_db == null) {
            _db = KurlDatabase.Companion(driver)
        }
    }

    val db: KurlDatabase
        get() = _db ?: error("AppDatabase not initialized. Call AppDatabase.init() at app startup.")
}

expect fun createDatabaseDriver(): SqlDriver