package com.moliveira.app.smartfridge.modules.sdk

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val sdkModule = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    isLenient = true
                })
            }
        }
    }
}