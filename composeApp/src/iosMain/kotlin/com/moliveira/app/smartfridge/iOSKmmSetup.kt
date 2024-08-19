package com.moliveira.app.smartfridge

import com.moliveira.app.smartfridge.database.cache.DatabaseDriverFactory
import com.moliveira.app.smartfridge.database.cache.IOSDatabaseDriverFactory
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

object IOSKmmSetup {
    fun setup() {
        Napier.base(DebugAntilog())
        startKoin {
            modules(
                this@IOSKmmSetup.modules()
            )
        }
    }

    private fun modules() = AppModule.modules() + listOf(platformModules())
}

private fun platformModules() = module {
    singleOf(::IOSDatabaseDriverFactory) { bind<DatabaseDriverFactory>() }
}
