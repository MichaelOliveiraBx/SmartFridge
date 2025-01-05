package com.moliveira.app.smartfridge.database.cache

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

class IOSDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createDriver(
        databaseName: String,
    ): SqlDriver = NativeSqliteDriver(CacheApiDatabase.Schema, databaseName)
}