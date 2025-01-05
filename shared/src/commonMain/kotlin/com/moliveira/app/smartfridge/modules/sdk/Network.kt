package com.moliveira.app.smartfridge.modules.sdk

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText

suspend inline fun <reified T> HttpResponse.handle(): Result<T> {
    return when (status.value) {
        in 200..299 -> {
            runCatching { body<T>() }
        }

        else -> {
            val body = bodyAsText()
            Result.failure(Exception("ERROR HTTP: ${status.value}", Exception(body)))
        }
    }
}