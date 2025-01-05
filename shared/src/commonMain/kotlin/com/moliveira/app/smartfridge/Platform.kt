package com.moliveira.app.smartfridge

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform