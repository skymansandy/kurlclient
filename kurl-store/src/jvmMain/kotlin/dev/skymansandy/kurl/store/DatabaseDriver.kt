package dev.skymansandy.kurl.store

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.skymansandy.kurlstore.db.KurlDatabase
import java.io.File

actual fun createDatabaseDriver(): SqlDriver {
    val dbFile = File(System.getProperty("user.home"), ".kurlclient/kurl.db")
    dbFile.parentFile?.mkdirs()
    val isNew = !dbFile.exists()
    val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
    if (isNew) {
        KurlDatabase.Companion.Schema.create(driver)
    }
    return driver
}