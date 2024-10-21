package com.moliveira.app.smartfridge

import android.content.Context
import com.moliveira.app.smartfridge.database.cache.AndroidDatabaseDriverFactory
import com.moliveira.app.smartfridge.database.cache.DatabaseDriverFactory
import com.moliveira.app.smartfridge.modules.sdk.DataStoreBuilder
import com.moliveira.app.smartfridge.modules.sdk.DataStoreBuilderPlatform
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.core.context.startKoin
import org.koin.dsl.bind
import org.koin.dsl.module

object AndroidKmmSetup {
    fun setup(
        appContext: Context,
    ) {
        Napier.base(DebugAntilog())
        startKoin {
            modules(
                modules(appContext)
            )
        }
    }

    private fun modules(
        appContext: Context,
    ) = AppModule.modules() + listOf(platformModules(appContext))
}

private fun platformModules(
    appContext: Context
) = module {
    single { AndroidDatabaseDriverFactory(appContext) } bind DatabaseDriverFactory::class
    factory { DataStoreBuilderPlatform(appContext) } bind DataStoreBuilder::class
}
