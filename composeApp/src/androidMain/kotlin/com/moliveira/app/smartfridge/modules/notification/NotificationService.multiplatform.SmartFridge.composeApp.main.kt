package com.moliveira.app.smartfridge.modules.notification

import android.content.Context
import com.moliveira.app.smartfridge.MainActivity.Companion.NOTIF_CHANNEL_ID
import com.moliveira.app.smartfridge.notification.AlarmScheduler
import com.moliveira.app.smartfridge.notification.NotificationDTO
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.DrawableResource
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class NotificationServicePlatform(
    private val appContext: Context,
    private val alarmScheduler: AlarmScheduler,
) : NotificationService {
    private val scope = GlobalScope

    private val _askForPermissionEventFlow = MutableSharedFlow<Unit>()
    val askForPermissionEvent: Flow<Unit> = _askForPermissionEventFlow

    override suspend fun scheduleNotification(
        title: String,
        body: String?,
        icon: DrawableResource?,
        localDateTime: LocalDateTime
    ): Result<String> {
        val randomId = (0..Int.MAX_VALUE).random()
        alarmScheduler.scheduleAlarmForReminder(
            data = NotificationDTO(
                title = title,
                id = randomId,
                date = localDateTime,
                description = body ?: "",
                channelId = NOTIF_CHANNEL_ID,
                channelVisibleName = "Smart Fridge"
            )
        )
        return Result.success(randomId.toString())
    }

    override suspend fun cancelNotification(id: String) {
        runCatching { id.toInt() }
            .onSuccess {
                alarmScheduler.removeAlarm(
                    context = appContext,
                    id = it
                )
            }
    }

    private var askForPermissionContinuation: Continuation<Boolean>? = null

    override suspend fun askForPermission(): Boolean = suspendCoroutine {
        askForPermissionContinuation = it
        scope.launch {
            _askForPermissionEventFlow.emit(Unit)
        }
    }

    fun onPermissionResult(granted: Boolean) {
        askForPermissionContinuation?.resume(granted)
        askForPermissionContinuation = null
    }

}