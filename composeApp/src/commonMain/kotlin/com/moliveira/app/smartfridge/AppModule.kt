package com.moliveira.app.smartfridge

import com.moliveira.app.smartfridge.modules.food.di.foodModule
import com.moliveira.app.smartfridge.modules.home.di.homeModule
import com.moliveira.app.smartfridge.modules.sdk.sdkModule
import org.koin.core.context.startKoin
import org.koin.core.module.Module

object AppModule {
    fun setup() {
        startKoin {
            modules(
                this@AppModule.modules()
            )
        }
    }

    fun modules() =
        listOf(
            sdkModule,
            foodModule,
            homeModule,
        )

}
