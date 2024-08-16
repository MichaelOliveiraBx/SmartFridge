package com.moliveira.app.smartfridge.database.cache

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

class AndroidDatabaseDriverFactory(
    private val appContext: Context,
) : DatabaseDriverFactory {
    override fun createDriver(databaseName: String): SqlDriver =
        AndroidSqliteDriver(CacheApiDatabase.Schema, appContext, databaseName)
}