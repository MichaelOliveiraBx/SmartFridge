package com.moliveira.app.smartfridge.modules.notification

import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.DrawableResource
import platform.Foundation.NSCalendar
import platform.Foundation.NSDateComponents
import platform.Foundation.NSUUID.Companion.UUID
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNCalendarNotificationTrigger.Companion.triggerWithDateMatchingComponents
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest.Companion.requestWithIdentifier
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNUserNotificationCenter.Companion.currentNotificationCenter
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

actual class NotificationServicePlatform : NotificationService {
    override suspend fun scheduleNotification(
        title: String,
        body: String?,
        icon: DrawableResource?,
        localDateTime: LocalDateTime,
    ): Result<String> {
        if (!check()) {
            return Result.failure(IllegalStateException("Notification permission not granted"))
        }

        val content = UNMutableNotificationContent()
        content.setTitle(title)
        body?.let { content.setBody(it) }

        val dateComponents = localDateTime.toNSDateComponents()

        val trigger = triggerWithDateMatchingComponents(
            dateComponents,
            false,
        )

        val uuidString = UUID().UUIDString
        val request = requestWithIdentifier(
            uuidString, content, trigger,
        )

        val notificationCenter = currentNotificationCenter()

        return suspendCoroutine { continuation ->
            notificationCenter.addNotificationRequest(request) { error ->
                if (error != null) {
                    continuation.resumeWithException(IllegalStateException(error.localizedDescription))
                } else {
                    continuation.resume(Result.success(uuidString))
                }
            }
        }
    }

    private suspend fun check() = suspendCoroutine<Boolean> { continuation ->
        UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
            options = UNAuthorizationOptionAlert or UNAuthorizationOptionBadge or UNAuthorizationOptionSound,
        ) { granted, error ->
            if (granted) {
                continuation.resume(true)
            } else {
                continuation.resume(false)
            }
        }
    }

    override suspend fun cancelNotification(id: String) {
        val notificationCenter = currentNotificationCenter()
        notificationCenter.removePendingNotificationRequestsWithIdentifiers(listOf(id))
    }
}

private fun LocalDateTime.toNSDateComponents(): NSDateComponents {
    val dateComponents = NSDateComponents()
    dateComponents.calendar = NSCalendar.currentCalendar
    dateComponents.minute = this.minute.toLong()
    dateComponents.hour = this.hour.toLong()
    dateComponents.year = this.year.toLong()
    dateComponents.month = this.monthNumber.toLong()
    dateComponents.day = this.dayOfMonth.toLong()
    dateComponents.second = this.second.toLong()
    return dateComponents
}