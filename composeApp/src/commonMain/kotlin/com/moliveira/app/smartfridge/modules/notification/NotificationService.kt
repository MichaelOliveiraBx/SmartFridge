package com.moliveira.app.smartfridge.modules.notification

import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.DrawableResource

interface NotificationService {
    suspend fun scheduleNotification(
        title: String,
        body: String?,
        icon: DrawableResource?,
        localDateTime: LocalDateTime,
    ): Result<String>

    suspend fun cancelNotification(id: String)
}

expect class NotificationServicePlatform constructor() : NotificationService