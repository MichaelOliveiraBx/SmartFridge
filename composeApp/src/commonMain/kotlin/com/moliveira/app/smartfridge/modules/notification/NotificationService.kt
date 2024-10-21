package com.moliveira.app.smartfridge.modules.notification

import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.DrawableResource

interface NotificationService {
    suspend fun scheduleNotification(
        title: String,
        body: String?,
        icon: DrawableResource?,
        localDateTime: LocalDateTime,
    ): Result<String>

    suspend fun cancelNotification(id: String)

    suspend fun askForPermission(): Boolean
}

expect class NotificationServicePlatform constructor() : NotificationService

val LocalNotificationService = staticCompositionLocalOf<NotificationService> {
    error("No NotificationService provided")
}

fun LocalDate.handleNotificationTime(): LocalDateTime? {
    val timeZone = TimeZone.currentSystemDefault()
    val now = Clock.System.now().toLocalDateTime(timeZone).date

    this.plus(DatePeriod(days = 1))
    val minus2Days = minus(DatePeriod(days = 2))
    val minus1Day = minus(DatePeriod(days = 1))

    return when {
        minus2Days > now -> minus2Days
        minus1Day > now -> minus1Day
        else -> null
    }?.let {
        LocalDateTime(
            it,
            LocalTime(
                hour = 11,
                minute = 0,
            )
        )
    }
}