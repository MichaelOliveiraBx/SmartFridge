package com.moliveira.app.smartfridge.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.moliveira.app.smartfridge.MainActivity
import com.moliveira.app.smartfridge.notification.utils.Constants
import io.github.aakira.napier.Napier
import kotlinx.serialization.json.Json

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Napier.d("onReceive() called with: context = [${context?.applicationContext}], intent = [$intent]")
        val appContext = context?.applicationContext ?: return
        Napier.d("${intent?.action}, ${intent?.extras}")
        val json = Json {
            isLenient = true
            ignoreUnknownKeys = true
        }

        if (intent != null && intent.action != null) {
            intent.extras?.let { extras ->
                runCatching {
                    extras.getString(Constants.NOTIFICATION_PARCELABLE_EXTRA)
                        ?.let { json.decodeFromString<NotificationDTO>(it) }
                        ?: throw IllegalStateException()
                }
                    .mapCatching { notification ->
                        Napier.d("notification:$notification")
                        val intentCreated = Intent(
                            appContext,
                            MainActivity::class.java
                        )
                            .setAction(Intent.ACTION_VIEW)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

                        NotificationHelper.createNotification(
                            context = context,
                            title = notification.title,
                            message = notification.description,
                            channelId = notification.channelId,
                            intent = intentCreated,
                            pendingIntentId = notification.id,
                        )
                    }
            }
        }
    }
}