package com.moliveira.app.smartfridge.modules.food.domain

import com.moliveira.app.smartfridge.modules.sdk.LocalizedString

data class FoodModel(
    val id: String,
    val name: LocalizedString,
    val thumbnail: String?,
)