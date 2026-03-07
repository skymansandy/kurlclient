package dev.skymansandy.kurl.store

import app.cash.sqldelight.db.SqlDriver

expect fun createDatabaseDriver(): SqlDriver
