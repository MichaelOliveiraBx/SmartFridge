package com.moliveira.app.smartfridge.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannelHelper {

    fun createNotificationChannel(context: Context, importance: Int, showBadge: Boolean, channelVisibleName: String, channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelVisibleName, importance)
            channel.setShowBadge(showBadge)
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}