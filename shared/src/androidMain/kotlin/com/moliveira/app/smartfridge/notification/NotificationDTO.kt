package com.moliveira.app.smartfridge.notification

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class NotificationDTO(
    val id: Int,
    val title: String? = null,
    val description: String,
    val date: LocalDateTime,
    val channelId: String = "",
    val channelVisibleName: String = "",
)

