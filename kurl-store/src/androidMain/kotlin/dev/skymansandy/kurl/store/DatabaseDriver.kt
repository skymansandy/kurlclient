package dev.skymansandy.kurl.store

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import dev.skymansandy.kurlstore.db.KurlDatabase

internal lateinit var appContext: Context

fun initAndroidContext(context: Context) {
    appContext = context.applicationContext
}

actual fun createDatabaseDriver(): SqlDriver =
    AndroidSqliteDriver(
        schema = KurlDatabase.Companion.Schema,
        context = appContext,
        name = "kurl.db",
        callback = object : AndroidSqliteDriver.Callback(KurlDatabase.Companion.Schema) {
            override fun onOpen(db: SupportSQLiteDatabase) {
                db.execSQL("PRAGMA foreign_keys = ON")
            }
        }
    )