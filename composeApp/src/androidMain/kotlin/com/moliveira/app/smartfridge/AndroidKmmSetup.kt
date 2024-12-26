package com.moliveira.app.smartfridge

import android.content.Context
import com.moliveira.app.smartfridge.database.cache.AndroidDatabaseDriverFactory
import com.moliveira.app.smartfridge.database.cache.DatabaseDriverFactory
import com.moliveira.app.smartfridge.modules.notification.NotificationService
import com.moliveira.app.smartfridge.modules.notification.NotificationServicePlatform
import com.moliveira.app.smartfridge.modules.sdk.DataStoreBuilder
import com.moliveira.app.smartfridge.modules.sdk.DataStoreBuilderPlatform
import com.moliveira.app.smartfridge.notification.AlarmScheduler
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.binds
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

object AndroidKmmSetup {
    fun setup(
        appContext: Context,
    ) {
        Napier.base(DebugAntilog())
        val koinApplication = startKoin {
            androidContext(appContext)
            modules(
                modules(appContext)
            )
        }
        AppModule.setup(koinApplication)
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
    single { Json { ignoreUnknownKeys = true } }
    singleOf(::AlarmScheduler)
    singleOf(::NotificationServicePlatform) {
        binds(listOf(NotificationServicePlatform::class, NotificationService::class))
    }
}
