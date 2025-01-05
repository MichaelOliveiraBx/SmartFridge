package com.moliveira.app.smartfridge.modules.sdk

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

class DataStoreBuilderPlatform(
    private val context: Context,
) : DataStoreBuilder {
    override fun build(filename: String): DataStore<Preferences> = createDataStore(
        producePath = { context.filesDir.resolve(filename).absolutePath }
    )
}