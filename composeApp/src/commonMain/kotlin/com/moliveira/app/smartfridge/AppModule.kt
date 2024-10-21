package com.moliveira.app.smartfridge

import com.moliveira.app.smartfridge.modules.food.di.foodModule
import com.moliveira.app.smartfridge.modules.home.di.homeModule
import com.moliveira.app.smartfridge.modules.sdk.SharedPrefs
import com.moliveira.app.smartfridge.modules.sdk.sdkModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module

object AppModule {
    lateinit var koinApplication: KoinApplication
    fun setup(
        koinApplication: KoinApplication
    ) {
        this.koinApplication = koinApplication

        koinApplication.koin.get<SharedPrefs>()
    }

    fun modules() =
        listOf(
            sdkModule,
            foodModule,
            homeModule,
        )

}
