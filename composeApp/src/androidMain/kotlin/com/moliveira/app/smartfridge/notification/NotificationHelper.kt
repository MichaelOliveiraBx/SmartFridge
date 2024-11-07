package com.moliveira.app.smartfridge.notification

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {

    @SuppressLint("MissingPermission")
    fun createNotification(
        context: Context,
        title: String?,
        message: String,
        channelId: String,
        intent: Intent,
        pendingIntentId: Int,
    ) {
        val notificationBuilder = NotificationCompat.Builder(context, channelId).apply {
//            setSmallIcon(R.drawable.ic_notification) // 3
            setContentTitle(title) // 4
            setContentText(message) // 5
            setGroupSummary(true)
            setGroup(channelId)
            setStyle(NotificationCompat.BigTextStyle().bigText(message))
            priority = NotificationCompat.PRIORITY_DEFAULT
            setAutoCancel(true)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            val pendingIntent =
                PendingIntent.getActivity(
                    context,
                    pendingIntentId,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            setContentIntent(pendingIntent)
        }
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(pendingIntentId, notificationBuilder.build())
    }
}