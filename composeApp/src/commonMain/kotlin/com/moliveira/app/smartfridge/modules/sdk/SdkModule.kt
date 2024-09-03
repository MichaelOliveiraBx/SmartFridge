package com.moliveira.app.smartfridge.modules.sdk

import com.moliveira.app.smartfridge.database.cache.DatabaseDriverFactory
import com.moliveira.app.smartfridge.modules.notification.NotificationService
import com.moliveira.app.smartfridge.modules.notification.NotificationServicePlatform
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val sdkModule = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }
    }
    singleOf(::NotificationServicePlatform) { bind<NotificationService>() }
}