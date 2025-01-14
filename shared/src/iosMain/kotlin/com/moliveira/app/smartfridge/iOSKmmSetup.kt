package com.moliveira.app.smartfridge

import androidx.core.bundle.Bundle
import com.moliveira.app.smartfridge.database.cache.DatabaseDriverFactory
import com.moliveira.app.smartfridge.database.cache.IOSDatabaseDriverFactory
import com.moliveira.app.smartfridge.modules.camera.KMMCameraRecognizerInterface
import com.moliveira.app.smartfridge.modules.notification.NotificationService
import com.moliveira.app.smartfridge.modules.notification.NotificationServicePlatform
import com.moliveira.app.smartfridge.modules.sdk.DataStoreBuilder
import com.moliveira.app.smartfridge.modules.sdk.DataStoreBuilderPlatform
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.configure
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.staticCFunction
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import platform.Foundation.NSBundle
import platform.Foundation.NSSetUncaughtExceptionHandler
import platform.posix.bind

object IOSKmmSetup {
    fun setup() {
        Napier.base(DebugAntilog())
        setupCrash()

        val koinApplication = startKoin {
            modules(
                this@IOSKmmSetup.modules()
            )
        }

        AppModule.setup(koinApplication)


        NSBundle.mainBundle.bundleIdentifier?.let {
            Napier.d("Bundle identifier: $it")
        } ?: run {
            Napier.e("Bundle identifier not found")
        }

    }

    private fun modules() = AppModule.modules() + listOf(platformModules())

    @OptIn(ExperimentalForeignApi::class)
    private fun setupCrash() {
        NSSetUncaughtExceptionHandler(
            staticCFunction { exception ->
                Napier.e("Uncaught exception: $exception")
            }
        )
    }
}

private fun platformModules() = module {
    singleOf(::IOSDatabaseDriverFactory) { bind<DatabaseDriverFactory>() }
    singleOf(::KMMCameraRecognizerInterface)
    factoryOf(::DataStoreBuilderPlatform) { bind<DataStoreBuilder>() }
    singleOf(::NotificationServicePlatform) { bind<NotificationService>() }
}
