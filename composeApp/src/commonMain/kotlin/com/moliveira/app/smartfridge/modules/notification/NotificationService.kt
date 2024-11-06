package com.moliveira.app.smartfridge.modules.notification

import androidx.compose.runtime.staticCompositionLocalOf
import com.moliveira.app.smartfridge.Res
import com.moliveira.app.smartfridge.notification_title_1
import com.moliveira.app.smartfridge.notification_title_2
import com.moliveira.app.smartfridge.notification_title_3
import io.github.aakira.napier.Napier
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.getString
import kotlin.random.Random

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

suspend fun notificationGetTitle() = getString(
    when (Random.nextInt(2)) {
        0 -> Res.string.notification_title_1
        1 -> Res.string.notification_title_2
        else -> Res.string.notification_title_3
    }
)

expect class NotificationServicePlatform constructor() : NotificationService

val LocalNotificationService = staticCompositionLocalOf<NotificationService> {
    error("No NotificationService provided")
}

fun LocalDate.handleNotificationTime(): LocalDateTime? {
    val timeZone = TimeZone.currentSystemDefault()
    val now = Clock.System.now().toLocalDateTime(timeZone).date

    val minus2Days = minus(DatePeriod(days = 2))
    val minus1Day = minus(DatePeriod(days = 1))

    Napier.d("now: $now minus2Days: $minus2Days minus1Day: $minus1Day")
    return when {
        minus2Days > now -> minus2Days
        minus1Day >= now -> minus1Day
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