package com.moliveira.app.smartfridge.database.cache

import app.cash.sqldelight.db.SqlDriver

interface DatabaseDriverFactory {
    fun createDriver(databaseName: String): SqlDriver
}
