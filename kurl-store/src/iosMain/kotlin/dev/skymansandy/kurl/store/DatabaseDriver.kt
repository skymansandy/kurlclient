package dev.skymansandy.kurl.store

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import dev.skymansandy.kurlstore.db.KurlDatabase

actual fun createDatabaseDriver(): SqlDriver =
    NativeSqliteDriver(KurlDatabase.Companion.Schema, "kurl.db")