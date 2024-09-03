package com.moliveira.app.smartfridge.modules.food.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

data class UserFoodModel(
    val id: String,
    val name: String,
    val thumbnail: String,
    val addAt: LocalDateTime,
    val expirationDate: LocalDate,
    val notificationId: String?,
)