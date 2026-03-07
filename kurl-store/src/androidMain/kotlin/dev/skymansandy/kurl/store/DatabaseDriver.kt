package dev.skymansandy.kurl.store

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import dev.skymansandy.store.db.KurlDatabase

internal lateinit var appContext: Context

fun initAndroidContext(context: Context) {
    appContext = context.applicationContext
}

actual fun createDatabaseDriver(): SqlDriver =
    AndroidSqliteDriver(KurlDatabase.Companion.Schema, appContext, "kurl.db")