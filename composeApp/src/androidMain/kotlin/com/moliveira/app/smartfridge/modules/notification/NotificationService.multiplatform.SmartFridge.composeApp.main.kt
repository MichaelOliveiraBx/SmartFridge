package com.moliveira.app.smartfridge.modules.notification

import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.DrawableResource

actual class NotificationServicePlatform : NotificationService {
    override suspend fun scheduleNotification(
        title: String,
        body: String?,
        icon: DrawableResource?,
        localDateTime: LocalDateTime
    ): Result<String> {
        TODO("Not yet implemented")
    }

    override suspend fun cancelNotification(id: String) {
        TODO("Not yet implemented")
    }

    override suspend fun askForPermission(): Boolean {
        TODO("Not yet implemented")
    }

}